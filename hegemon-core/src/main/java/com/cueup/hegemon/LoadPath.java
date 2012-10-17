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

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * A LoadPath contains a list of ScriptLocators to be searched in order to load a file.
 *
 * The first script locator is always the "system" locator.
 */
public class LoadPath {
  private final List<ScriptLocator> paths;

  private static final ScriptLocator SYSTEM_PATH = new ResourceScriptLocator(LoadPath.class, "/javascript");


  /**
   * The default path contains only a resource loader for the '/javascript' directory in resources.
   */
  public static LoadPath defaultPath() {
    return new LoadPath(Collections.singletonList(SYSTEM_PATH));
  }


  // TODO(kevinclark): probably worth having a helper for appending to the default path, but no direct need yet.

  /**
   * A custom path allows the user to inject locators *before and after* the system path. That means that users
   * can potentially override hegemon standard libraries. It's useful for hegemon-testing's HegemonTestRunner
   * because it needs to reload actual *source* files instead of resources moved to the classes directory.
   */
  public static LoadPath customPath(List<ScriptLocator> beforeSystem, List<ScriptLocator> afterSystem) {
    List<ScriptLocator> paths = Lists.newArrayList();
    paths.addAll(beforeSystem);
    paths.add(SYSTEM_PATH);
    paths.addAll(afterSystem);

    return new LoadPath(paths);
  }

  /**
   * Creates a new LoadPath that searches sequentially for a given file. Passed locators are appended to the default
   * load path, which searches resource directories at '/javascript'.
   * @param locators the ScriptLocators.
   */
  private LoadPath(List<ScriptLocator> locators) {
    this.paths = locators;
  }

  /**
   * Returns the contents of a file found in this LoadPath.
   * @param name the name of the file to try to locate.
   * @return the String contents of the found file.
   * @throws LoadError if no file is found.
   */
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
