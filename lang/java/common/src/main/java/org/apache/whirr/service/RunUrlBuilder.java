package org.apache.whirr.service;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import org.jclouds.scriptbuilder.ScriptBuilder;

public class RunUrlBuilder {

  // Need to be able to specify base URL
  // Perhaps make these scripts parameterizable?
  // e.g. just java/install then base url is .../openjdk or .../sun or
  // .../apache or .../cloudera
  public static byte[] runUrls(String... urls) {
    ScriptBuilder scriptBuilder = new ScriptBuilder().addStatement(
      exec("wget -qO/usr/bin/runurl run.alestic.com/runurl")).addStatement(
      exec("chmod 755 /usr/bin/runurl"));

    for (String url : urls) {
      scriptBuilder.addStatement(exec("runurl cloudera-tom.s3.amazonaws.com/"
        + url));
    }

    return scriptBuilder.build(org.jclouds.scriptbuilder.domain.OsFamily.UNIX)
      .getBytes();
  }

}
