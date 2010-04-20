package org.apache.whirr.service.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HadoopProxy {

  private HadoopCluster cluster;
  private Process process;
  
  public HadoopProxy(HadoopCluster cluster) {
    this.cluster = cluster;
  }

  public void start() throws IOException {
    // jsch doesn't support SOCKS-based dynamic port forwarding, so we need to shell out...
    // TODO: Use static port forwarding instead?
    String identityFile = cluster.getServiceSpec().getSecretKeyFile();
    String user = "ubuntu"; // TODO: get from jclouds
    String server = cluster.getNamenodePublicAddress().getHostName();
    String[] command = new String[] { "ssh",
	"-i", identityFile,
	"-o", "ConnectTimeout=10",
	"-o", "ServerAliveInterval=60",
	"-o", "StrictHostKeyChecking=no",
	"-N",
	"-D 6666",
	String.format("%s@%s", user, server)};
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    process = processBuilder.start();
    
    final BufferedReader errReader = 
      new BufferedReader(new InputStreamReader(process.getErrorStream()));
    
    Thread errThread = new Thread() {
      @Override
      public void run() {
        try {
          String line = errReader.readLine();
          while((line != null) && !isInterrupted()) {
            System.err.println(line);
            line = errReader.readLine();
          }
        } catch(IOException e) {
          e.printStackTrace();
        }
      }
    };
    errThread.start();
  }
  
  public void stop() {
    process.destroy();
  }
  
}
