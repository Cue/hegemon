/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ResourceScriptLocator extends  ScriptLocator {
  private final File rootDir;

  protected ResourceScriptLocator() {
    this.rootDir = new File("");
  }

  protected ResourceScriptLocator(String rootDir) {
    this.rootDir = new File(rootDir);
  }

  @Override
  public URL getFile(String name) {
    try {

      return new URL(getClass().getResource("/"), new File(this.rootDir, name).getPath());
    } catch (MalformedURLException e) {
      return null;
    }
  }
}
