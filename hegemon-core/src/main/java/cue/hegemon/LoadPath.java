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

package cue.hegemon;

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


  /**
   * Creates a new LoadPath that searches.
   * @param locators the ScriptLocators to be searched (sequentially) for a given file.
   */
  public LoadPath(ScriptLocator ... locators) {
    this.paths = Lists.newLinkedList();
    this.paths.add(new ResourceScriptLocator(getClass(), "/javascript"));

    if (locators != null && locators.length > 0) {
      Collections.addAll(this.paths, locators);
    }
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
