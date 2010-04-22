package org.apache.whirr.service;

import java.net.InetAddress;
import java.util.Properties;
import java.util.Set;

public class Cluster {
  
  public static class Instance {
    private Set<String> roles;
    private InetAddress publicAddress;
    private InetAddress privateAddress;

    public Instance(Set<String> roles, InetAddress publicAddress,
	InetAddress privateAddress) {
      this.roles = roles;
      this.publicAddress = publicAddress;
      this.privateAddress = privateAddress;
    }

    public Set<String> getRoles() {
      return roles;
    }

    public InetAddress getPublicAddress() {
      return publicAddress;
    }

    public InetAddress getPrivateAddress() {
      return privateAddress;
    }
    
  }
  
  private Set<Instance> instances;
  private Properties configuration;

  public Cluster(Set<Instance> instances) {
    this(instances, new Properties());
  }

  public Cluster(Set<Instance> instances, Properties configuration) {
    this.instances = instances;
    this.configuration = configuration;
  }

  public Set<Instance> getInstances() {
    return instances;
  }  
  public Properties getConfiguration() {
    return configuration;
  }

}
