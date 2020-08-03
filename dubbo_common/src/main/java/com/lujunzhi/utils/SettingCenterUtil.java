package com.lujunzhi.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.InputStream;
import java.util.Properties;

public class SettingCenterUtil extends PropertyPlaceholderConfigurer {
    static {
       initConfig();
    }
    public static void initConfig(){
        try {
            String driver;
            String user;
            String password;
            String url;
            InputStream resourceAsStream = SettingCenterUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            url = properties.getProperty("jdbc.url");
            user = properties.getProperty("jdbc.user");
            password = properties.getProperty("jdbc.password");
            driver = properties.getProperty("jdbc.driver");
            //创建重试策略
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,1);
            //创建客户端
            CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 3000, retryPolicy);
            client.start();
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.url",url.getBytes());
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.user",user.getBytes());
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.password",password.getBytes());
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.driver",driver.getBytes());

            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 载入zookeeper数据
     * @param props
     */
    private void loadZk(Properties props){
        //创建重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,1);
        //创建客户端
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 3000, retryPolicy);
        client.start();
        try {
            byte[] urlBytes = client.getData().forPath("/config/jdbc.url");
            props.setProperty("jdbc.url",new String(urlBytes));
            byte[] userBytes = client.getData().forPath("/config/jdbc.user");
            props.setProperty("jdbc.user",new String(userBytes));
            byte[] pwdBytes = client.getData().forPath("/config/jdbc.password");
            props.setProperty("jdbc.password",new String(pwdBytes));
            byte[] driverBytes = client.getData().forPath("/config/jdbc.driver");
            props.setProperty("jdbc.driver",new String(driverBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }
    /**
     * 处理properties内容,相当于此标签
     *  <context:property-placeholder location="classpath:jdbc.properties"></context:property-placeholder>
     * @param beanFactoryToProcess
     * @param props
     * @throws BeansException
     */
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        // 载入zookeeper配置信息，即从Zookeeper中获取数据源的连接信息
        loadZk(props);
        addWatch(props);
        super.processProperties(beanFactoryToProcess, props);
    }
    /**
     * 添加watch机制
     * @param props
     */
    private void addWatch(Properties props) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,1);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 3000, retryPolicy);
        client.start();
        //创建缓存机制，监听 /config 路径
        TreeCache treeCache = new TreeCache(client,"/config");
        //启动缓存机制
        try {
            treeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //添加监听
        treeCache.getListenable().addListener(new TreeCacheListener() {

            /**
             *
             * @param client  zookeeper客户端对象
             * @param event  zookeeper改变触发的事件
             * @throws Exception
             */
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                //如果是修改事件则，更新数据
                if(event.getType() == TreeCacheEvent.Type.NODE_UPDATED){
                    //如果修改的是jdbc.url路径，修改jdbc.url的配置
                    if(event.getData().getPath().equals("/config/jdbc.url")){
                        props.setProperty("jdbc.url",new String(event.getData().getData()));
                    }else if(event.getData().getPath().equals("/config/jdbc.driver")){
                        props.setProperty("jdbc.driver",new String(event.getData().getData()));
                    }else if(event.getData().getPath().equals("/config/jdbc.user")){
                        props.setProperty("jdbc.user",new String(event.getData().getData()));
                    }else if(event.getData().getPath().equals("/config/jdbc.password")){
                        props.setProperty("jdbc.password",new String(event.getData().getData()));
                    }
                }
            }
        });
    }

    @Override
    public void setPropertiesArray(Properties... propertiesArray) {
        super.setPropertiesArray(propertiesArray);
    }

}
