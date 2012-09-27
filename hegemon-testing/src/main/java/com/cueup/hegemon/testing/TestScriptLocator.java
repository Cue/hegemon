/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon.testing;

import com.cueup.hegemon.ResourceScriptLocator;
import java.net.URL;

/**
 * Locates scripts for testing using resource locations.
 */
public class TestScriptLocator extends ResourceScriptLocator {

  @Override
  public URL getScript(String name) {
    return getFile(name + ".js");
  }

  public TestScriptLocator() {
    super("../../src/main/webapp/");
  }
}

