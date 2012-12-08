/*
 * Copyright 2012 the hegemon authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cueup.hegemon;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Simple script locator that just loads scripts under a sub-path.
 * This is useful for development environments.  For production code,
 * you almost certainly want ResourceScriptLocator.
 */
public class PathScriptLocator extends ScriptLocator {

  private final File root;

  /**
   * Creates a path locator with the given root path.
   * @param root the root path
   */
  public PathScriptLocator(String root) {
    this(new File(root));
  }

  /**
   * Creates a path locator with the given root path.
   * @param root the root path
   */
  public PathScriptLocator(File root) {
    this.root = root;
  }

  @Override
  public String getFile(String name) throws LoadError {
    File script = new File(this.root, name);
    try {
      if (script.getCanonicalPath().startsWith(this.root.getCanonicalPath())
          && script.exists()) {
        return Files.toString(script, Charsets.UTF_8);
      }
    } catch (IOException e) {
      throw new LoadError(e);
    }
    throw new LoadError("Could not find " + name + " under " + this.root);
  }

}
