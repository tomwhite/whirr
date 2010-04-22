package org.apache.whirr.service.zookeeper;

import java.util.Set;

import org.apache.whirr.service.Cluster;

public class ZooKeeperCluster extends Cluster {
  
  private String hosts;

  public ZooKeeperCluster(Set<Instance> instances, String hosts) {
    super(instances);
    this.hosts = hosts;
  }
  
  public String getHosts() {
    return hosts;
  }

}
