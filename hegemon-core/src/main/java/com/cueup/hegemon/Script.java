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

  /**
   * Logging.
   */
  private static final Log LOG = LogFactory.getLog(Script.class);

  /**
   * Allows for user defined script location.
   */
  private final LoadPath loadPath;

  /**
   * The 'global' scope for this script.
   * Scripts are evaluated in this context.
   */
  private final Scriptable sharedScope;

  /**
   * Whether a script has already been loaded into this context.
   */
  private final Set<String> loaded;

  /**
   * Where we keep values that need to exist cross script invocations.
   */
  @ReferencedByJavascript
  public static final Cache<ValueName, Object> STATIC_VALUES =
      CacheBuilder.newBuilder().build();


  // TODO(kevinclark): lambda l: try: l(enterContext()) finally: exitContext()
  // Use these wrappers instead of Context.enter / Context.exit
  // to ensure correct version is used.


  /**
   * Enter a new lexical context.
   * @return the context object.
   */
  public static Context enterContext() {
    final Context context = Context.enter();
    context.setLanguageVersion(Context.VERSION_1_8);
    return context;
  }


  /**
   * Exit the current context.
   */
  public static void exitContext() {
    Context.exit();
  }


  /**
   * Load a new script context from a source, found with a locator,
   * loading globalFiles.
   * @param source - The source code to be run.
   * @param loadPath - How to find any files loaded.
   * @param globalFiles - Files to load to run this source.
   * @throws LoadError when files don't load properly.
   */
  public Script(final String source, final LoadPath loadPath,
                final String... globalFiles) throws LoadError {
    this.loadPath = loadPath;
    this.loaded = Sets.newHashSet();

    Context context = enterContext();
    try {
      this.sharedScope = context.initStandardObjects();
      ScriptableObject.putProperty(this.sharedScope, "log",
          Context.javaToJS(LOG, this.sharedScope));
      ScriptableObject.putProperty(this.sharedScope, "hegemon",
          Context.javaToJS(this, this.sharedScope));

      for (String globalFile : globalFiles) {
        load(globalFile);
      }

      context.evaluateString(this.sharedScope, source, "main", 1, null);
    } finally {
      exitContext();
    }
  }


  /**
   * Load the script located with the Script's loadPath with the given filename.
   * @param scriptName - the name of the script to load (sans .js).
   * @throws LoadError when unable to load the associated resource.
   */
  public void load(final String scriptName) throws LoadError {
    if (this.loaded.contains(scriptName)) {
      return;
    }
    this.loaded.add(scriptName);

    String filename = scriptName + ".js";
    Context context = enterContext();
    try {
      String code = this.loadPath.load(filename);
      context.evaluateString(this.sharedScope, code, filename, 1, null);
    } finally {
      exitContext();
    }
  }


  /**
   * Returns the source in the given filename.
   * @param filename the source to load.
   * @return the text in the source file.
   * @throws LoadError when unable to load the associated resource.
   */
  public String read(final String filename) throws LoadError {
    return this.loadPath.load(filename);
  }


  /**
   * Run the given function by name in the current context.
   * @param functionName - the name of the function to run.
   * @param values - the arguments passed to the function.
   * @return the result of the function call.
   */
  public Object run(final String functionName, final Object... values) {
    // Create a local copy of the bindings so we can multi-thread.
    Context context = enterContext();

    try {
      final Scriptable localScope = context.newObject(this.sharedScope);
      localScope.setPrototype(this.sharedScope);
      localScope.setParentScope(null);

      List<String> names = Lists.newArrayList();
      for (int i = 0; i < values.length; i++) {
        final Object value = values[i];
        if (value instanceof String || value instanceof Number
            || value instanceof Boolean || value instanceof Scriptable) {
          ScriptableObject.putProperty(localScope, "__p" + i, values[i]);
        } else {
          ScriptableObject.putProperty(localScope, "__p" + i,
              Context.javaToJS(values[i], localScope));
        }
        names.add("__p" + i);
      }
      String code = functionName + "(" + Joiner.on(",").join(names) + ");";
      return unwrap(context.evaluateString(localScope, code, "main", 1, null));
    } finally {
      exitContext();
    }
  }

  /**
   * Unwrap the object return from the js runtime.
   *
   * Cribbed from com.sun.phobos.script.javascript.ExternalScriptable.java
   * BSD licensed
   *
   * @param jsObj the object to unwrap.
   * @return the unwrapped object.
   */
  private Object unwrap(final Object jsObj) {
    if (jsObj instanceof Wrapper) {
      Wrapper njb = (Wrapper) jsObj;

      if (njb instanceof NativeJavaClass) {
        return njb;
      }

      Object obj = njb.unwrap();
      if (obj instanceof Number || obj instanceof String
          || obj instanceof Boolean || obj instanceof Character) {
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

