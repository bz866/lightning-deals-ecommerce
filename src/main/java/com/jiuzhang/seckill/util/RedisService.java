package com.jiuzhang.seckill.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;

@Slf4j
@Service
public class RedisService {
    @Autowired
    private JedisPool jedisPool;
    /**
     * Redis Setter
     *
     * @param key * @param value */
    public void setValue(String key, Long value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value.toString());
        jedisClient.close();
    }
    /**
     * Redis Getter
     *
     * @param key * @return */
    public String getValue(String key) {
        Jedis jedisClient = jedisPool.getResource();
        String value = jedisClient.get(key);
        jedisClient.close();
        return value;
    }

    /**
     * inventory decrement by Lua in Redis
     *
     * @return
     * @throws Exception
     * */
    public boolean stockDeductValidator(String key)  {
        try(Jedis jedisClient = jedisPool.getResource()) {
            // atomic operation by Lua
            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "                 local stock = tonumber(redis.call('get', KEYS[1]))\n" +
                    "                 if( stock <=0 ) then\n" +
                    "                    return -1\n" +
                    "                 end;\n" +
                    "                 redis.call('decr',KEYS[1]);\n" +
                    "                 return stock - 1;\n" +
                    "             end;\n" +
                    "             return -1;";
            Long stock = (Long) jedisClient.eval(script, Collections.singletonList(key), Collections.emptyList());
            if (stock < 0) {
                System.out.println("库存不足");
                return false;
            } else {
                System.out.println("恭喜，抢购成功");
            }
            return true;
        } catch (Throwable throwable) {
            System.out.println("库存扣减失败：" + throwable.toString());
            return false;
        }
    }

    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }

    /*
    Check if a userId is in the order limit table
     */
    public boolean isInLimitMember(long seckillActivityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        boolean sismember = jedisClient.sismember("seckillActivity_users:" + seckillActivityId, String.valueOf(userId));
        jedisClient.close();
        log.info("userId:{} activityId:{} 在已购名单中:{}", userId, seckillActivityId, sismember);
        return sismember;
    }

    /*
    Add userId to order limit table after order created
     */
    public void addLimitMember(long seckillActivityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.sadd("seckillActivity_users:" + seckillActivityId, String.valueOf(userId));
        jedisClient.close();
    }

    public void removeLimitMember(Long seckillActivityId, Long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.srem("seckillActivity_users:" + seckillActivityId, String.valueOf(userId));
        jedisClient.close();
    }

    /**
     * get the distributed locks
     * @param lockKey
     * @param requestId
     * @param expireTime
     * @return
     */
    public  boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {
        Jedis jedisClient = jedisPool.getResource();
        String result = jedisClient.set(lockKey, requestId, "NX", "PX",
                expireTime);
        jedisClient.close();
        if ("OK".equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * release the distributed locks
     * @param lockKey
     * @param requestId
     * @return boolean, if releasing successful
     */
    public boolean releaseDistributedLock(String lockKey, String requestId) {
        Jedis jedisClient = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Long result = (Long) jedisClient.eval(
                script,
                Collections.singletonList(lockKey),
                Collections.singletonList(requestId)
        );
        jedisClient.close();
        if (result == 1L) {
            return true;
        }
        return false;
    }
}
