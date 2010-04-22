package org.apache.whirr.service.hadoop;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.net.InetAddress;
import java.util.Properties;
import java.util.Set;

import org.apache.whirr.service.Cluster;

public class HadoopCluster extends Cluster {
  public HadoopCluster(Set<Instance> instances, Properties configuration) {
    super(instances, configuration);
  }
  public InetAddress getNamenodePublicAddress() {
    return Iterables.getOnlyElement(Sets.filter(getInstances(), new Predicate<Instance>() {
      @Override
      public boolean apply(Instance instance) {
	return instance.getRoles().contains("nn");
      }
    })).getPublicAddress();
  }
}
