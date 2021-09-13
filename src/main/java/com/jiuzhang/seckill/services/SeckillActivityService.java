package com.jiuzhang.seckill.services;

import com.alibaba.fastjson.JSON;
import com.jiuzhang.seckill.db.dao.OrderDao;
import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.dao.SeckillCommodityDao;
import com.jiuzhang.seckill.db.po.Order;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.db.po.SeckillCommodity;
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

    @Autowired
    SeckillCommodityDao seckillCommodityDao;

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

        // 1.Generate new Order object
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        // set order details
        // Generate the Order ID by SnowFlake Generator
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());

        // 2. create task/message the RocketMQ to create order, asyn to finish the task
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        // 3. send delayed message to the RocketMQ
        // messageDelayLevel = 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        // check payment status after 10s
        rocketMQService.sendDelayMessage("seckill_order", JSON.toJSONString(order), 3);
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
     * push lighting deal infomation into redis
     * @param seckillActivityId
     */
    public void pushSeckillInfoToRedis(long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:" + seckillActivityId, JSON.toJSONString(seckillActivity));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(), JSON.toJSONString(seckillCommodity));
    }

    /**
     * 订单支付完成处理 * @param orderNo */
    public void payOrderProcess(String orderNo) throws Exception {
        log.info("完成支付订单 订单号:" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        /*
         * 1. check if order existed
         * 2. check if the payment status of the order is unpaid
         * */
        if (order == null) {
            log.error("订单号对应订单不存在:" + orderNo);
            return;
        } else if(order.getOrderStatus() != 1 ) {
            log.error("订单状态无效:" + orderNo);
            return;
        }
        /*
        check if the order payment successful
         */
        order.setPayTime(new Date());
        // order status
        // 0: no available inventory, invalid order
        // 1: order created, pending for payment
        // 2: payment success, order.setOrderStatus(2);
        orderDao.updateOrder(order);
        /*
         * 3. return message that payment success
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }
}
