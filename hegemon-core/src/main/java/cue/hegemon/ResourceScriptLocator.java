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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A ScriptLocator which finds files using getResource.
 */
public class ResourceScriptLocator extends ScriptLocator {
  private final File rootDir;
  private final Class klass;


  /**
   * Creates a locator that will load using the .getResource method of the given class
   * at the toplevel resources directory.
   * @param klass the class to load resources from.
   */
  public ResourceScriptLocator(Class klass) {
    this(klass, "");
  }

  /**
   * Creates a locator that will load using the .getResource method of the given class
   * at the given rootDirectory.
   * @param klass the class to load resources from.
   * @param rootDir the directory to load files from.
   */
  public ResourceScriptLocator(Class klass, String rootDir) {
    this.klass = klass;
    this.rootDir = new File(rootDir);
  }


  /**
   * Returns the string contents of the given file name, if found.
   * @param name the file name to read.
   * @return the String contents of the file.
   * @throws LoadError when the file is not found.
   */
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
