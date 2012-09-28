/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ResourceScriptLocator extends  ScriptLocator {
  private final File rootDir;
  private final Class klass;

  public ResourceScriptLocator(Class klass) {
    this(klass, "");
  }

  public ResourceScriptLocator(Class klass, String rootDir) {
    this.klass = klass;
    this.rootDir = new File(rootDir);
  }

  @Override
  public String getFile(String name) throws LoadError {
    try {

      String path = new File("/", new File(this.rootDir, name).getPath()).getPath();
      URL resource = this.klass.getResource(path);
      if (resource == null) {
        throw new LoadError("Unable to load: " + name);
      }
      return Resources.toString(resource, Charsets.UTF_8);
    } catch (MalformedURLException e) {
      throw new LoadError(e);
    } catch (IOException e) {
      throw new LoadError(e);
    }
  }
}
