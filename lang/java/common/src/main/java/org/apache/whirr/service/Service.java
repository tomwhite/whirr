package org.apache.whirr.service;

import java.io.IOException;

import org.jclouds.compute.ComputeService;

public abstract class Service {
  protected ServiceSpec serviceSpec;

  public Service(ServiceSpec serviceSpec) {
    this.serviceSpec = serviceSpec;
  }
  
  public abstract Cluster launchCluster(ClusterSpec clusterSpec)
    throws IOException;
  
  public void destroyCluster() throws IOException {
    ComputeService computeService = ComputeServiceBuilder.build(serviceSpec);
    computeService.destroyNodesWithTag(serviceSpec.getClusterName());
  }

}
