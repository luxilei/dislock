package com.lxl.zk.lock;

import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author : lxl
 * @version : 1.0
 * @date : 2019/4/10
 */
public interface DistributedLock {

    /**获取锁，如果没有得到就等待*/
    void acquire() throws Exception;

    /**
     * 获取锁，直到超时
     * @param time 超时时间
     * @param unit time参数的单位
     * @return是否获取到锁
     * @throws Exception
     */
    boolean acquire(long time, TimeUnit unit) throws Exception;

    /**
     * 释放锁
     * @throws Exception
     */
    void release()  throws Exception;
}