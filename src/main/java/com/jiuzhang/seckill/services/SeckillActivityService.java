package com.jiuzhang.seckill.services;

import com.jiuzhang.seckill.util.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

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
}
