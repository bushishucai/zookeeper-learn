package com.msb.zookeeper.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * @author: 马士兵教育
 * @create: 2019-09-20 20:21
 */
public class WatchCallBack  implements Watcher ,AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    ZooKeeper zk ;
    MyConf conf ;
    CountDownLatch cc = new CountDownLatch(1);

    public MyConf getConf() {
        return conf;
    }

    public void setConf(MyConf conf) {
        this.conf = conf;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }


    public void aWait(){
        zk.exists("/AppConf",this,this ,"ABC");
        System.out.println("await");
        try {
            //在这停住，想往下走只能
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //getData的回调----DataCallback
    //只要调用aWait方法===>exists方法===>exists回调===>getData方法===>getData的回调
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        //数据为""，会countDown，aWait方法出栈，进行下一次while循环
        //数据为null，不会countDown，程序卡在cc.await()，直到有事件发生，触发watcher回调
        if(data != null ){
            String s = new String(data);
            conf.setConf(s);
            cc.countDown();
        }
    }
    //exists的回调----StatCallback
    //只要调用aWait方法===>exists方法===>exists回调===>getData方法===>getData的回调
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if(stat != null){
            //getData不是真的为了获取数据，而是为了触发getData的回调，为conf服务，执行countDown，返回结果给主线程
            zk.getData("/AppConf",this,this,"sdfs");
        }

    }

    //Watcher
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                //getData不是真的为了获取数据，而是为了触发getData的回调，为conf服务，执行countDown，返回结果给主线程
                zk.getData("/AppConf",this,this,"sdfs");

                break;
            case NodeDeleted:
                //容忍性
                //conf设为""，CountDownLatch重新设为1，主线程会再次执行aWait，并阻塞，直到有数据
                conf.setConf("");
                cc = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                //getData不是真的为了获取数据，而是为了触发getData的回调，为conf服务，执行countDown，返回结果给主线程
                zk.getData("/AppConf",this,this,"sdfs");
                break;
            case NodeChildrenChanged:
                break;
        }

    }
}
