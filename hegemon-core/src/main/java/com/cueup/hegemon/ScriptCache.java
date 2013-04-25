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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.script.ScriptException;
import java.util.concurrent.ExecutionException;

/**
 * Cache of scripts.
 */
public class ScriptCache {

  private final LoadingCache<String, Script> cache;


  /**
   * Create a ScriptCache that loads scripts from the given LoadPath.
   * @param loadPath the LoadPath to load files from.
   */
  public ScriptCache(final LoadPath loadPath) {
    this(loadPath, 1);
  }


  /**
   * Create a ScriptCache that loads scripts from the given LoadPath.
   * @param loadPath the LoadPath to load files from.
   * @param optimizationLevel the optimization level to use for cached scripts.
   */
  public ScriptCache(final LoadPath loadPath, final int optimizationLevel) {
    this.cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Script>() {
      @Override
      public Script load(String key) throws Exception {
        return new Script(key, loadPath.load(key), optimizationLevel, loadPath);
      }
    });
  }


  /**
   * Clear the cache.
   */
  public void clear() {
    this.cache.invalidateAll();
  }


  /**
   * Get a script, optionally reloading.
   * @param script the name of the script to load.
   * @param reload whether or not to reload the script cleanly.
   * @return the Script with the given name.
   * @throws LoadError if the script file can't be located.
   * @throws ScriptException if the script doesn't evaluate correctly.
   */
  public Script get(String script, boolean reload) throws LoadError, ScriptException {
    try {
      if (reload) {
        this.cache.invalidate(script);
      }
      return this.cache.get(script);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ScriptException) {
        throw (ScriptException) e.getCause();
      } else if (e.getCause() instanceof LoadError) {
        throw (LoadError) e.getCause();
      } else if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      } else {
        throw new RuntimeException("Impossible exception", e);
      }
    }
  }


  /**
   * Get a cached script.
   * @param script the name of the script to load.
   * @return the Script with the given name.
   * @throws LoadError if the script file can't be located.
   * @throws ScriptException if the script doesn't evaluate correctly.
   */
  public Script get(String script) throws LoadError, ScriptException {
    return get(script, false);
  }
}

