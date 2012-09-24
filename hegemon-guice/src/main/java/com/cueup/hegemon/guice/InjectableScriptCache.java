/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon.guice;

import com.cueup.hegemon.Script;
import com.cueup.hegemon.ScriptCache;
import com.cueup.hegemon.ScriptLocator;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.script.ScriptException;
import java.io.IOException;


/**
 * A ScriptCache suitable for injection.
 */
@Singleton
public class InjectableScriptCache extends ScriptCache {

  @Inject
  public InjectableScriptCache(final ScriptLocator locator) {
    super(locator);
  }


  @Override
  public Script get(String script, boolean reload) throws IOException, ScriptException {
    return super.get(script, reload);
  }
}
