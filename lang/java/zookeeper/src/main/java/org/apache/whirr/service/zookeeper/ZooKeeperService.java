package org.apache.whirr.service.zookeeper;

import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.whirr.service.ComputeServiceBuilder;
import org.apache.whirr.service.RunUrlBuilder;
import org.apache.whirr.service.ServiceSpec;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;

public class ZooKeeperService {
    
  private static final int CLIENT_PORT = 2181;
  
  private ServiceSpec spec;
  
  public ZooKeeperService(ServiceSpec serviceSpec) {
    this.spec = serviceSpec;
  }

  public String launchCluster(int ensembleSize) throws IOException {
      
    ComputeService computeService = ComputeServiceBuilder.build(spec);

    byte[] bootScript = RunUrlBuilder.runUrls(
	"sun/java/install",
	"apache/zookeeper/install");
    Template template = computeService.templateBuilder()
      .osFamily(OsFamily.UBUNTU)
      .options(runScript(bootScript)
	  .installPrivateKey(spec.readPrivateKey())
	  .authorizePublicKey(spec.readPublicKey())
	  .inboundPorts(22, CLIENT_PORT))
      .build();
    
    Map<String, ? extends NodeMetadata> nodeMap =
      computeService.runNodesWithTag(spec.getClusterName(), ensembleSize,
	  template);
    List<NodeMetadata> nodes = Lists.newArrayList(nodeMap.values());
    
    // Pass list of all servers in ensemble to configure script.
    // Position is significant: i-th server has id i.
    String servers = Joiner.on(' ').join(getPrivateIps(nodes));
    byte[] configureScript = RunUrlBuilder.runUrls(
	"apache/zookeeper/post-configure " + servers);
    computeService.runScriptOnNodesWithTag(spec.getClusterName(), configureScript);
    
    return Joiner.on(',').join(getHosts(nodes));
  }

  private List<String> getPrivateIps(List<NodeMetadata> nodes) {
    return Lists.transform(Lists.newArrayList(nodes),
	new Function<NodeMetadata, String>() {
      @Override
      public String apply(NodeMetadata node) {
	return Iterables.get(node.getPrivateAddresses(), 0).getHostAddress();
      }
    });
  }
  
  private List<String> getHosts(List<NodeMetadata> nodes) {
    return Lists.transform(Lists.newArrayList(nodes),
	new Function<NodeMetadata, String>() {
      @Override
      public String apply(NodeMetadata node) {
	String publicIp =  Iterables.get(node.getPublicAddresses(), 0)
	  .getHostName();
	return String.format("%s:%d", publicIp, CLIENT_PORT);
      }
    });
  }

  public void destroyCluster() throws IOException {
    ComputeService computeService = ComputeServiceBuilder.build(spec);
    computeService.destroyNodesWithTag(spec.getClusterName());
  }
}
