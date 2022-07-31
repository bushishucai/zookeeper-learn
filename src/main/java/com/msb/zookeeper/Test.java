package com.msb.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        CountDownLatch cd = new CountDownLatch(1);
        final ZooKeeper zk = new ZooKeeper(
                //可以配置多个节点，会随机连接一个
                "127.0.0.1:2181",
                3000,
                event -> {
                    Watcher.Event.EventType type = event.getType();
                    Watcher.Event.KeeperState state = event.getState();
                    System.out.println("session watch");
                    switch (state) {
                        case Unknown:
                            break;
                        case Disconnected:
                            break;
                        case NoSyncConnected:
                            break;
                        case SyncConnected:
                            cd.countDown();
                            break;
                        case AuthFailed:
                            break;
                        case ConnectedReadOnly:
                            break;
                        case SaslAuthenticated:
                            break;
                        case Expired:
                            break;
                    }
                });
        cd.await();
        ZooKeeper.States state = zk.getState();
        System.out.println(state);

        //异步
        zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, (i, s, o, stat1) -> System.out.println(o), "create异步");


        final Stat stat = new Stat();
        //回调
        //byte[] data = zk.getData("/ooxx", new Watcher() {
        //    @Override
        //    public void process(WatchedEvent watchedEvent) {
        //        System.out.println(watchedEvent.toString() + "，触发getData回调");
        //        try {
        //            zk.getData("/ooxx", this, stat);
        //        } catch (KeeperException | InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //    }
        //}, stat);

        //异步 + 回调
        zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("getData回调");
                try {
                    zk.getData("/ooxx", this, stat);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, (i, s, o, bytes, stat12) -> System.out.println(o), "getData异步");

        //异步
        //zk.setData("/ooxx", "data".getBytes(), 0,(i, s, o, s1) -> System.out.println(o),"setData异步");
        //zk.setData("/ooxx", "data".getBytes(), 1,(i, s, o, s1) -> System.out.println(o),"setData异步");

        //同步
        //Stat stat1 = zk.setData("/ooxx", "set data".getBytes(), 0);
        //zk.setData("/ooxx", "new data".getBytes(), stat1.getVersion());

        Thread.sleep(10000);
    }
}
