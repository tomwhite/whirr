package org.apache.whirr.service;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;

import java.io.IOException;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;

public class ComputeServiceBuilder {

  public static ComputeService build(ServiceSpec spec) throws IOException {
    Set<AbstractModule> wiring = ImmutableSet.of(new JschSshClientModule(),
      new Log4JLoggingModule());

    ComputeServiceContext context = new ComputeServiceContextFactory()
      .createContext(spec.getProvider(), spec.getAccount(), spec.getKey(),
        wiring);

    return context.getComputeService();
  }
}
