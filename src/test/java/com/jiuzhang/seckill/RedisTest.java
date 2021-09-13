package com.jiuzhang.seckill;

import com.jiuzhang.seckill.services.SeckillActivityService;
import com.jiuzhang.seckill.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.UUID;


@SpringBootTest
public class RedisTest {

    @Resource
    private RedisService redisService;

    @Autowired
    SeckillActivityService seckillActivityService;

    @Test
    public void  stockTest(){
        redisService.setValue("stock:19",10L);
    }

    @Test
    public void getStockTest(){
        String stock =  redisService.getValue("stock:19");
        System.out.println(stock);
    }

    @Test
    public void stockDeductValidatorTest(){
        boolean result =  redisService.stockDeductValidator("stock:19");
        System.out.println("result:"+result);
        String stock =  redisService.getValue("stock:19");
        System.out.println("stock:"+stock);
    }

    @Test
    public void pushSeckillInfoToRedisTest() {
        seckillActivityService.pushSeckillInfoToRedis(19);
    }

    /**
     * test the Get method of the distributed locks under the high concurrency scenario
     */
    @Test
    public void  testConcurrentAddLock() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // print result; true false false false false false false false false false
            // only the first request get the lock
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
        }
    }

    /**
     * test the Release method of the distributed locks under the high concurrency scenario
     */
    @Test
    public void  testConcurrent() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // print result; true true true true true true true true true true
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
            redisService.releaseDistributedLock("A", requestId);
        } }
}
