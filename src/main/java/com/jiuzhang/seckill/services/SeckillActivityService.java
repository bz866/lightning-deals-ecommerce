package com.jiuzhang.seckill.services;

import com.alibaba.fastjson.JSON;
import com.jiuzhang.seckill.db.dao.OrderDao;
import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.po.Order;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.mq.RocketMQService;
import com.jiuzhang.seckill.util.RedisService;
import com.jiuzhang.seckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    OrderDao orderDao;

    /**
     * datacenterId;
     * machineId;
     * Fetch from the configuration in distributed system
     * static value in local
     */
    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * create order
     *
     * @param seckillActivityId
     * @param userId
     * @return
     * @throws Exception
     */
    public Order createOrder(long seckillActivityId, long userId) throws Exception {
        /*
         * 1.Create Order */
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        // Generate the Order ID by SnowFlake Generator
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        /*
        *2. Send order details to the RocketMQ
        */
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));
        return order;
    }

    /**
     * check if any inventory available
     * @param activityId
     *
     * @return
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }

    /**
     * 订单支付完成处理 * @param orderNo */
    public void payOrderProcess(String orderNo) {
        log.info("完成支付订单 订单号:" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        boolean deductStockResult = seckillActivityDao.deductStock(order.getSeckillActivityId());
        if (deductStockResult) {
            order.setPayTime(new Date());
            // orders status:
            // 0: no available inventory，invalid order
            // 1: order created, pending for payment
            // 2: payment complemented
            orderDao.updateOrder(order);
        }
    }
}
