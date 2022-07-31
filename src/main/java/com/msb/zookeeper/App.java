package com.msb.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");

        //zk是有session概念的，没有连接池的概念
        //watch:观察，回调
        //watch的注册只发生在读类型的操作如: get，exites。。。等等， 写类型的操作是产生事件的
        //第一类Watch：new zk 时候，传入的watch，这个watch，session级别的，跟path 、node没有关系，session有什么事情，他会被调起
        final CountDownLatch cd = new CountDownLatch(1);
        final ZooKeeper zk = new ZooKeeper(
                //可以配置多个节点，会随机连接一个
                "127.0.0.1:2181",
                3000,
                new Watcher() {
                    //Watch 的回调方法！
                    @Override
                    public void process(WatchedEvent event) {
                        Event.KeeperState state = event.getState();
                        Event.EventType type = event.getType();
                        String path = event.getPath();
                        System.out.println("new zk watch: " + event.toString());

                        //当watch里的process方法被回调的时候，可以看状态和类型
                        switch (state) {
                            case Unknown:
                                break;
                            case Disconnected:
                                break;
                            case NoSyncConnected:
                                break;
                            case SyncConnected:
                                System.out.println("connected");
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

                        switch (type) {
                            case None:
                                break;
                            case NodeCreated:
                                break;
                            case NodeDeleted:
                                break;
                            case NodeDataChanged:
                                break;
                            case NodeChildrenChanged:
                                break;
                        }


                    }
                });
        //创建zk连接的时候，它会很快的给我们返回一个对象，这时的状态是CONNECTING，所以要这样处理
        //和前面的 cd.countDown()配合，保证代码执行到这里，一定已经建立连接了
        cd.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("ing......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed........");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        /**
         * String path 路径
         * byte[] data 二进制数据流
         * List<ACL> acl 权限控制
         * CreateMode createMode 创建模式 四种 (持久/临时)*(普通/带序列)
         */
        String pathName = zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        final Stat stat = new Stat();
        //getData有四个重载方法，(同步/异步)*(带Watch/不带Watch)
        //第二类Watch，监控指定路径上的事件
        byte[] node = zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watch: " + event.toString());
                try {
                    //1. watch为true的时候，default Watch(new zk时的那个watch)被重新注册
                    //zk.getData("/ooxx", true, stat);
                    //2. 和上面是重载方法，参数不一样，watcher使用this
                    //   path的watch是一次性的，再事件回调里在加入一个
                    zk.getData("/ooxx", this, stat);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println(new String(node));

        //触发回调
        Stat stat1 = zk.setData("/ooxx", "newdata".getBytes(), 0);
        //还会触发吗？
        //Stat stat2 = zk.setData("/ooxx", "newdata01".getBytes(), stat1.getVersion()); //注意version是从上一次的Stat获取的

        //实行顺序是start--over--back，不会阻塞
        System.out.println("-------async start----------");
        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("-------async call back----------");
                System.out.println(ctx.toString());
                System.out.println(new String(data));
            }
        }, "abc");
        System.out.println("-------async over----------");


        Thread.sleep(100000);
    }
}
