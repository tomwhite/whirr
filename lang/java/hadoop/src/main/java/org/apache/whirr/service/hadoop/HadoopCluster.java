package org.apache.whirr.service.hadoop;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.whirr.service.ServiceSpec;

public class HadoopCluster {
  private ServiceSpec serviceSpec;
  private InetAddress namenodePublicAddress;
  private Properties hadoopSiteProperties;
  
  public HadoopCluster(ServiceSpec serviceSpec,
	InetAddress namenodePublicAddress, Properties hadoopSiteProperties) {
    this.serviceSpec = serviceSpec;
    this.namenodePublicAddress = namenodePublicAddress;
    this.hadoopSiteProperties = hadoopSiteProperties;
  }
  public ServiceSpec getServiceSpec() {
    return serviceSpec;
  }
  public InetAddress getNamenodePublicAddress() {
      return namenodePublicAddress;
  }
  public Properties getHadoopSiteProperties() {
      return hadoopSiteProperties;
  }
}
