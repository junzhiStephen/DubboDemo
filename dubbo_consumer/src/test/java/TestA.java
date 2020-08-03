import com.lujunzhi.utils.SettingCenterUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Properties;

public class TestA {
    @Test
    public void test1(){


            try {
                String driver;
                String user;
                String password;
                String url;
                FileInputStream fileInputStream = new FileInputStream("D:\\Dubbo\\dubbo_provider\\src\\main\\resources\\jdbc.properties");
                //InputStream resourceAsStream = SettingCenterUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
                Properties properties = new Properties();
                properties.load(fileInputStream);
                url = properties.getProperty("jdbc.url");
                user = properties.getProperty("jdbc.user");
                password = properties.getProperty("jdbc.password");
                driver = properties.getProperty("jdbc.driver");
                //创建重试策略
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000,1);
                //创建客户端
                CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 3000, retryPolicy);
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config","".getBytes());
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.url",url.getBytes());
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.user",user.getBytes());
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.password",password.getBytes());
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/config/jdbc.driver",driver.getBytes());

                client.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

