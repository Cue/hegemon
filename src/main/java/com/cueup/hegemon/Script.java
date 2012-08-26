/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * A pre-parsed script.
 */
public class Script {

  private static final Log log = LogFactory.getLog(Script.class);

  private final ScriptLocator locator;

  private final Scriptable sharedScope;

  private final Set<String> loaded;
// TODO(kevinclark): Re-enable this. Requires extracting KeyedTimers from greplin common.
//                   Plus metrics dependency.
//  @ReferencedByJavascript
//  public static final KeyedTimers<String> TIMERS = new KeyedTimers<String>(
//      Script.class, "jsTimers", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);


  @ReferencedByJavascript
  public static final Cache<ValueName, Object> STATIC_VALUES = CacheBuilder.newBuilder().build();


  // TODO(kevinclark): lambda l: try: l(enterContext()) finally: exitContext()
  // Use these wrappers instead of Context.enter / Context.exit to ensure correct version is used.
  public static Context enterContext() {
    final Context context = Context.enter();
    context.setLanguageVersion(Context.VERSION_1_8);
    return context;
  }

  public static void exitContext() {
    Context.exit();
  }

  public Script(String source, ScriptLocator locator, String... globalFiles) throws IOException {
    this.locator = locator;
    this.loaded = Sets.newHashSet();

    Context context = enterContext();
    try {
      this.sharedScope = context.initStandardObjects();
      ScriptableObject.putProperty(this.sharedScope, "log", Context.javaToJS(log, this.sharedScope));
      ScriptableObject.putProperty(this.sharedScope, "hegemon", Context.javaToJS(this, this.sharedScope));

      for (String globalFile : globalFiles) {
        load(globalFile);
      }

      context.evaluateString(this.sharedScope, source, "main", 1, null);
    } finally {
      exitContext();
    }
  }

  public void load(String filename) throws IOException {
    if (this.loaded.contains(filename)) {
      return;
    }
    this.loaded.add(filename);

    Context context = enterContext();
    try {
      URL resource = this.locator.getScript(filename);
      if (resource == null) {
        throw new RuntimeException("Can't locate resource for " + filename);
      }
      String code = Resources.toString(resource, Charsets.UTF_8);
      context.evaluateString(this.sharedScope, code, filename + ".js", 1, null);
    } finally {
      exitContext();
    }
  }

  public String read(String filename) throws IOException {
    return Resources.toString(this.locator.getFile(filename), Charsets.UTF_8);
  }

  public Object run(String functionName, Object... values) {
    // Create a local copy of the bindings so we can multi-thread.
    Context context = enterContext();

    try {
      final Scriptable localScope = context.newObject(this.sharedScope);
      localScope.setPrototype(this.sharedScope);
      localScope.setParentScope(null);

      List<String> names = Lists.newArrayList();
      for (int i = 0; i < values.length; i++) {
        final Object value = values[i];
        if (value instanceof String || value instanceof Number || value instanceof Boolean
            || value instanceof Scriptable) {
          ScriptableObject.putProperty(localScope, "__p" + i, values[i]);
        } else {
          ScriptableObject.putProperty(localScope, "__p" + i, Context.javaToJS(values[i], localScope));
        }
        names.add("__p" + i);
      }
      String code = functionName + "(" + Joiner.on(",").join(names) + ");";
      return unwrap(context.evaluateString(localScope, code, "main", 1, null));
    } finally {
      exitContext();
    }
  }


  // Unwrap the object return from the js runtime.
  //
  // Cribbed from com.sun.phobos.script.javascript.ExternalScriptable.java
  // BSD licensed
  private Object unwrap(final Object jsObj) {
    if (jsObj instanceof Wrapper) {
      Wrapper njb = (Wrapper) jsObj;

      if (njb instanceof NativeJavaClass) {
        return njb;
      }

      Object obj = njb.unwrap();
      if (obj instanceof Number || obj instanceof String || obj instanceof Boolean || obj instanceof Character) {
        // special type wrapped -- we just leave it as is.
        return njb;
      } else {
        // return unwrapped object for any other object.
        return obj;
      }
    } else {
      return jsObj;
    }
  }
}

