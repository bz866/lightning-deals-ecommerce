package com.jiuzhang.seckill.util;

/**
 * SnowFlake ID Generator from Twitter
 * Distributed Identifier Generator
 * Ref: https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake
 *
 **/
public class SnowFlake {
    /**
     * starting timestamp */
    private final static long START_STMP = 1480166465631L;

    /**
     * bits for different parts */
    private final static long SEQUENCE_BIT = 12;
    private final static long MACHINE_BIT = 5;
    private final static long DATACENTER_BIT = 5;

    /**
     * maximum of each part */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * left shift of each part */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
    private long datacenterId; // data center
    private long machineId; // machine
    private long sequence = 0L; // sequential id
    private long lastStmp = -1L;// the last time stamp

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * generate the next ID *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }
        if (currStmp == lastStmp) { // auto-increment in the same millisecond
            sequence = (sequence + 1) & MAX_SEQUENCE; // maximum amount reached in the same millisecond
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        }
        else { // initial the sequential id as 0 in each millisecond,
            sequence = 0L;
        }
        lastStmp = currStmp;
        return (currStmp - START_STMP) << TIMESTMP_LEFT // timestamp part
                | datacenterId << DATACENTER_LEFT // data center part
                | machineId << MACHINE_LEFT // machine part
                | sequence; // sequential part
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.out.println(snowFlake.nextId());
        }
        System.out.println("总耗时:" + (System.currentTimeMillis() - start));
    }
}