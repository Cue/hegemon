/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Cache of scripts.
 */
public class ScriptCache {

  private final LoadingCache<String, Script> cache;

  public ScriptCache(final LoadPath loadPath) {
    this.cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Script>() {
      @Override
      public Script load(String key) throws Exception {
        // TODO(robbyw): Dependencies should be managed elsewhere.
        return new Script(loadPath.load(key), loadPath, "hegemon/core");
      }
    });
  }

  public void clear() {
    this.cache.invalidateAll();
  }

  public Script get(String script, boolean reload) throws IOException, ScriptException {
    try {
      if (reload) {
        this.cache.invalidate(script);
      }
      return this.cache.get(script);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ScriptException) {
        throw (ScriptException) e.getCause();
      } else if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      } else {
        throw new RuntimeException("Impossible exception", e);
      }
    }
  }
}

