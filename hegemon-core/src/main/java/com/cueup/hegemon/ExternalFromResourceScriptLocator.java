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
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class shouldn't be used in production. It's intended to allow reloading of source
 * outside of compiled resources. It was created for HegemonTestServer implementations,
 * and probably doesn't belong anywhere else. Use at your own risk.
 *
 * It takes a directory and appends it to the first root directory of a getResources() call,
 * which might be your 'target/test-classes' directory. This is in contrast to the
 * ResourceScriptLocator, which appends your path to the locator's root directory *before*
 * passing it to getResource.
 */
public class ExternalFromResourceScriptLocator extends ResourceScriptLocator {
  /**
   * Create a new ExternalFromResourceScriptLocator.
   */
  public ExternalFromResourceScriptLocator(Class klass, String rootDir) {
    super(klass, rootDir);
  }

  @Override
  public String getFile(String name) throws LoadError {
    try {
      URL resourceDir = getKlass().getResource("/");
      File script = new File(resourceDir.getFile(), new File(getRootDir(), name).getPath());
      return Resources.toString(script.toURL(), Charsets.UTF_8);
    } catch (MalformedURLException e) {
      throw new LoadError("Unable to load: " + name, e);
    } catch (IOException e) {
      throw new LoadError("Unable to load: " + name, e);
    }
  }
}
