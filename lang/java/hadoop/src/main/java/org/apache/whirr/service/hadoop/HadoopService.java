package org.apache.whirr.service.hadoop;

import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.whirr.service.ComputeServiceBuilder;
import org.apache.whirr.service.RunUrlBuilder;
import org.apache.whirr.service.ServiceSpec;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;

public class HadoopService {

  private ServiceSpec spec;

  public HadoopService(ServiceSpec spec) {
    this.spec = spec;
  }

  public HadoopCluster launchCluster(int numWorkers) throws IOException {
    ComputeService computeService = ComputeServiceBuilder.build(spec);

    String privateKey = spec.readPrivateKey();
    String publicKey = spec.readPublicKey();
    
    // deal with user packages and autoshutdown with extra runurls
    byte[] nnjtBootScript = RunUrlBuilder.runUrls(
      "sun/java/install",
      String.format("apache/hadoop/install nn,jt -c %s", spec.getProvider()));
    
    Template template = computeService.templateBuilder()
    .osFamily(OsFamily.UBUNTU)
    .options(runScript(nnjtBootScript)
        .installPrivateKey(privateKey)
	      .authorizePublicKey(publicKey)
	      .inboundPorts(22, 80, 8020, 8021, 50030)) // TODO: restrict further
    .build();
    
    Map<String, ? extends NodeMetadata> nodes = computeService.runNodesWithTag(
	    spec.getClusterName(), 1, template);
    NodeMetadata node = Iterables.getOnlyElement(nodes.values());
    InetAddress namenodePublicAddress = Iterables.getOnlyElement(node.getPublicAddresses());
    InetAddress jobtrackerPublicAddress = Iterables.getOnlyElement(node.getPublicAddresses());
    
    byte[] slaveBootScript = RunUrlBuilder.runUrls(
      "sun/java/install",
      String.format("apache/hadoop/install dn,tt -n %s -j %s",
	      namenodePublicAddress.getHostName(),
	      jobtrackerPublicAddress.getHostName()));

    template = computeService.templateBuilder()
    .osFamily(OsFamily.UBUNTU)
    .options(runScript(slaveBootScript)
        .installPrivateKey(privateKey)
	      .authorizePublicKey(publicKey))
    .build();

    computeService.runNodesWithTag(spec.getClusterName(), numWorkers, template);
    
    // TODO: wait for TTs to come up (done in test for the moment)
    
    Properties config = createClientSideProperties(namenodePublicAddress, jobtrackerPublicAddress);
    return new HadoopCluster(spec, namenodePublicAddress, config);
  }
  
  public void destroyCluster() throws IOException {
    ComputeService computeService = ComputeServiceBuilder.build(spec);
    computeService.destroyNodesWithTag(spec.getClusterName());
  }
  
  private Properties createClientSideProperties(InetAddress namenode, InetAddress jobtracker) throws IOException {
      Properties config = new Properties();
      config.setProperty("hadoop.job.ugi", "root,root");
      config.setProperty("fs.default.name", String.format("hdfs://%s:8020/", namenode.getHostName()));
      config.setProperty("mapred.job.tracker", String.format("%s:8021", jobtracker.getHostName()));
      config.setProperty("hadoop.socks.server", "localhost:6666");
      config.setProperty("hadoop.rpc.socket.factory.class.default", "org.apache.hadoop.net.SocksSocketFactory");
      return config;
  }

  private void createClientSideHadoopSiteFile(InetAddress namenode, InetAddress jobtracker) throws IOException {
    File file = new File("/tmp/hadoop-site.xml");
    Files.write(generateHadoopConfigurationFile(createClientSideProperties(namenode, jobtracker)), file, Charsets.UTF_8);
  }
  
  private CharSequence generateHadoopConfigurationFile(Properties config) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\"?>\n");
    sb.append("<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n");
    sb.append("<configuration>\n");
    for (Entry<Object, Object> entry : config.entrySet()) {
      sb.append("<property>\n");
      sb.append("<name>").append(entry.getKey()).append("</name>\n");
      sb.append("<value>").append(entry.getValue()).append("</value>\n");
      sb.append("</property>\n");
    }
    sb.append("</configuration>\n");
    return sb;
  }
  
}
