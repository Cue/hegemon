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

import com.cueup.hegemon.annotations.ReferencedByJavascript;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import java.util.List;
import java.util.Map;
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
  private final Scriptable localScope;

  /**
   * Whether a script has already been loaded into this context.
   */
  private final Set<String> loaded;

  private final Map<String, Object> moduleCache;

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

  private static final Scriptable PARENT_SCOPE;

  static {
    Context context = enterContext();
    try {
      PARENT_SCOPE = context.initStandardObjects();
    } finally {
      exitContext();
    }
  }


  /**
   * Load a new script from source with the default load path.
   * @param source - The source code to be run.
   * @throws LoadError when files don't load properly.
   */
  public Script(final String source) throws LoadError {
    this(source, LoadPath.defaultPath());
  }

  /**
   * Load a new script context from a source, found with a locator,
   * loading globalFiles. 'hegemon/core' is loaded by default.
   * @param source - The source code to be run.
   * @param loadPath - How to find any files loaded.
   * @param globalFiles - Files to load to run this source.
   * @throws LoadError when files don't load properly.
   */
  public Script(final String source, final LoadPath loadPath,
                final String... globalFiles) throws LoadError {
    this.loadPath = loadPath;
    this.loaded = Sets.newHashSet();
    this.moduleCache = Maps.newHashMap();


    Context context = enterContext();
    try {
      this.localScope = context.newObject(PARENT_SCOPE);
      putCoreObjects(this.localScope);

      ScriptableObject.putProperty(this.localScope, "core",
          Context.javaToJS(load("hegemon/core"), this.localScope));

      // Put via moduleNameFor and putProperty
      for (String globalFile : globalFiles) {
        String moduleName = moduleNameFor(globalFile);
        ScriptableObject.putProperty(this.localScope, moduleName, load(globalFile));
      }

      context.evaluateString(this.localScope, source, "main", 1, null);
    } finally {
      exitContext();
    }
  }


  void putCoreObjects(Scriptable scope) {
    ScriptableObject.putProperty(scope, "log", Context.javaToJS(LOG, scope));
    ScriptableObject.putProperty(scope, "hegemon", Context.javaToJS(this, scope));
  }

  /**
   * Load the script located with the Script's loadPath with the given filename.
   * @param scriptName - the name of the script to load (sans .js).
   * @throws LoadError when unable to load the associated resource.
   */
  public Object load(final String scriptName) throws LoadError {
    // if we've already loaded it, return it
    if (this.loaded.contains(scriptName)) {
      return this.moduleCache.get(scriptName);
    }
    this.loaded.add(scriptName);

    String filename = scriptName + ".js";
    String moduleName = moduleNameFor(scriptName);
    Context context = enterContext();
    try {
      Scriptable newScope = context.newObject(PARENT_SCOPE);
      putCoreObjects(newScope);
      String code = this.loadPath.load(filename);
      context.evaluateString(newScope, code, filename, 1, null);
      try {
        Object preWrap = context.evaluateString(newScope, moduleName, "import " + moduleName, 1, null);
        Object module = unwrap(preWrap);
        this.moduleCache.put(scriptName, module);
        return module;
      } catch (EcmaError e) {
        if (!e.getMessage().startsWith("ReferenceError")) {
          throw e;
        } else {
          return null;
        }
      }
    } finally {
      exitContext();
    }
  }

  private String moduleNameFor(final String scriptName) {
    String[] parts = scriptName.split("[/\\\\]");
    return parts[parts.length - 1];
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
      final Scriptable localScope = context.newObject(this.localScope);
      localScope.setPrototype(this.localScope);
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

