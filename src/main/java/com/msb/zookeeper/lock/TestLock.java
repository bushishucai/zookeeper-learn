package com.msb.zookeeper.lock;

import com.msb.zookeeper.config.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: 马士兵教育
 * @create: 2019-09-20 21:21
 */
public class TestLock {


    ZooKeeper zk ;

    @Before
    public void conn (){
        zk  = ZKUtils.getZK();
    }

    @After
    public void close (){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lock(){
        //这10个线程可能出现在10台不同的机器里
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                WatchCallBack watchCallBack = new WatchCallBack();
                watchCallBack.setZk(zk);
                String threadName = Thread.currentThread().getName();
                watchCallBack.setThreadName(threadName);
                //每一个线程：
                //抢锁
                watchCallBack.tryLock();
                //干活(所有的业务代码都写在这里)
                System.out.println(threadName+" working...");
                    try {
                        Thread.sleep(100000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                //释放锁
                watchCallBack.unLock();
            }).start();
        }
        while(true){
        }
    }
}
