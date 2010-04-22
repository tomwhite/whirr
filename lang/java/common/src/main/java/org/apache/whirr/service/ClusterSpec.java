package org.apache.whirr.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ClusterSpec {
  
  public static class InstanceTemplate {
    private Set<String> roles;
    private int numberOfInstances;

    public InstanceTemplate(int numberOfInstances, String... roles) {
      this(numberOfInstances, Sets.newHashSet(roles));
    }

    public InstanceTemplate(int numberOfInstances, Set<String> roles) {
      this.numberOfInstances = numberOfInstances;
      this.roles = roles;
    }

    public Set<String> getRoles() {
      return roles;
    }

    public int getNumberOfInstances() {
      return numberOfInstances;
    }
    
  }
  
  private Properties configuration;
  private List<InstanceTemplate> instanceTemplates;
  private Map<Set<String>, InstanceTemplate> instanceTemplatesMap = Maps.newHashMap();
  
  public ClusterSpec(InstanceTemplate... instanceTemplates) {
    this(Arrays.asList(instanceTemplates));
  }

  public ClusterSpec(List<InstanceTemplate> instanceTemplates) {
    this(new Properties(), instanceTemplates);
  }

  public ClusterSpec(Properties configuration, List<InstanceTemplate> instanceTemplates) {
    this.configuration = configuration;
    this.instanceTemplates = instanceTemplates;
    for (InstanceTemplate template : instanceTemplates) {
      instanceTemplatesMap.put(template.roles, template);
    }
  }

  public Properties getConfiguration() {
    return configuration;
  }

  public List<InstanceTemplate> getInstanceTemplates() {
    return instanceTemplates;
  }
  
  public InstanceTemplate getInstanceTemplate(Set<String> roles) {
    return instanceTemplatesMap.get(roles);
  }
  
  public InstanceTemplate getInstanceTemplate(String... roles) {
    return getInstanceTemplate(Sets.newHashSet(roles));
  }
  
}
