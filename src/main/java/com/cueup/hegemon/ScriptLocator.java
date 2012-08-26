/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import java.net.URL;

/**
 * Interface for a class that can locate scripts.
 */
public abstract class ScriptLocator {

  URL getScript(String name) {
    return getFile(name + ".js");
  }

  abstract URL getFile(String name);
}
