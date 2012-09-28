/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class LoadPath {
  private List<ScriptLocator> paths;

  public LoadPath(ScriptLocator ... locators) {
    this.paths = Lists.newLinkedList();
    this.paths.add(new ResourceScriptLocator(getClass(), "/javascript"));

    if (locators != null && locators.length > 0) {
      Collections.addAll(this.paths, locators);
    }
  }

  public String load(String name) throws LoadError {
    for (ScriptLocator path : this.paths) {
      try {
        return path.getFile(name);
      } catch (LoadError loadError) {
        // Just keep trying
      }
    }

    throw new LoadError("Unable to load: " + name);
  }
}
