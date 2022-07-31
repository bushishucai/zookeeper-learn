package com.msb.zookeeper.config;

/**
 * @author: 马士兵教育
 * @create: 2019-09-20 20:28
 */

//这个class使你未来最关心的地方
//用于工作线程和回调线程之间传输数据(两个线程共享一个对象)
public class MyConf {

    private  String conf ;

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }
}
