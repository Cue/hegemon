/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon.guice;

import com.cueup.hegemon.LoadError;
import com.cueup.hegemon.LoadPath;
import com.cueup.hegemon.Script;
import com.cueup.hegemon.ScriptCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.script.ScriptException;


/**
 * A ScriptCache suitable for injection.
 */
@Singleton
public class InjectableScriptCache extends ScriptCache {

  @Inject
  public InjectableScriptCache(final LoadPath loadPath) {
    super(loadPath);
  }


  @Override
  public Script get(String script, boolean reload) throws LoadError, ScriptException {
    return super.get(script, reload);
  }
}
