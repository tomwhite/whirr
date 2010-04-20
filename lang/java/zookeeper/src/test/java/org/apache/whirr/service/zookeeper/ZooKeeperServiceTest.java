package org.apache.whirr.service.zookeeper;

import static com.google.common.base.Preconditions.checkNotNull;
import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.whirr.service.ServiceSpec;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZooKeeperServiceTest {
  
  private String clusterName = "zkclustertest";
  
  private ZooKeeperService service;
  private String hosts;
  
  @Before
  public void setUp() throws IOException {
    String secretKeyFile;
    try {
       secretKeyFile = checkNotNull(System.getProperty("whirr.test.ssh.keyfile"));
    } catch (NullPointerException e) {
       secretKeyFile = System.getProperty("user.home") + "/.ssh/id_rsa";
    }
    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setProvider(checkNotNull(System.getProperty("whirr.test.provider", "ec2")));
    serviceSpec.setAccount(checkNotNull(System.getProperty("whirr.test.user")));
    serviceSpec.setKey(checkNotNull(System.getProperty("whirr.test.key")));
    serviceSpec.setSecretKeyFile(secretKeyFile);
    serviceSpec.setClusterName(clusterName);
    service = new ZooKeeperService(serviceSpec);
    hosts = service.launchCluster(2);
    System.out.println(hosts);
  }
  
  @Test
  public void test() throws Exception {
    class ConnectionWatcher implements Watcher {

      private ZooKeeper zk;
      private CountDownLatch latch = new CountDownLatch(1);
      
      public void connect(String hosts) throws IOException, InterruptedException {
	zk = new ZooKeeper(hosts, 5000, this);
	latch.await();
      }
      
      public ZooKeeper getZooKeeper() {
	return zk;
      }
      
      @Override
      public void process(WatchedEvent event) {
	if (event.getState() == KeeperState.SyncConnected) {
	  latch.countDown();
	}
      }
      
      public void close() throws InterruptedException {
	if (zk != null) {
	  zk.close();
	}
      }
      
    }
    
    String path = "/data";
    String data = "Hello";
    ConnectionWatcher watcher = new ConnectionWatcher();
    watcher.connect(hosts);
    watcher.getZooKeeper().create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE,
      CreateMode.PERSISTENT);
    watcher.close();
    
    watcher = new ConnectionWatcher();
    watcher.connect(hosts);
    byte[] actualData = watcher.getZooKeeper().getData(path, false, null);
    assertEquals(data, new String(actualData));
    watcher.close();
  }
  
  @After
  public void tearDown() throws IOException {
    service.destroyCluster();
  }
  
}
