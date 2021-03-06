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
import com.cueup.hegemon.compilation.CachedScriptCompilation;
import com.cueup.hegemon.compilation.ScriptCompilation;
import com.cueup.hegemon.compilation.SimpleScriptCompilation;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Script objects are the basic interface to running JavaScript using hegemon.
 *
 * In it's simplest usage, Script can be instantiated given a name and some source code and
 * then function that exist in the Script's environment can be accessed via the 'run' method.
 *
 * Script also introduces a concept of 'module loading' to JavaScript. If you would like
 * symbols defined in a file to be accessible for other files, define a variable with the
 * same name as the file it's located in and attach 'public' values to it. For example,
 * in a file named 'foo.js', one might export a function named 'bar' like so:
 *
 *     let foo = {};
 *     foo.bar = function() { };
 *
 * Given this foo.js, another file can load the 'foo' object using 'core.load':
 *
 *     let foo = core.load('foo'); // Aliasing can happen by changing variable names
 *     foo.bar(); // I can now access anything that was attached to foo in foo.js.
 *
 * 'core.load' will return null when no explicit public object is defined. Loaded modules
 * resulting from 'core.load' calls are cached and the source is not recompiled. Note that
 * this only applies to loads from within the source file, however. If Script objects with
 * the same name and source are going to be built multiple times, using a ScriptCache is
 * probably preferable.
 */
public class Script {

  /**
   * Logging.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Script.class);

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

  /**
   * Whether a script is currently loading.
   */
  private final Set<String> loading;

  private final Map<String, Object> moduleCache;

  private final ScriptCompilation scriptCompilation;


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
   * @param name - The name of the script.
   * @param source - The source code to be run.
   * @throws LoadError when files don't load properly.
   */
  public Script(final String name, final String source) throws LoadError {
    this(name, source, LoadPaths.defaultPath());
  }

  /**
   * @see Script#Script(String, String, ScriptCompilation, LoadPath, String...)
   *
   * @param name - The name of the script.
   * @param source - The source code to be run.
   * @param loadPath - How to find any files loaded.
   * @param globalFiles - Files to load to run this source.
   * @throws LoadError when files don't load properly.
   */
  public Script(final String name,
                final String source,
                final LoadPath loadPath,
                final String... globalFiles) throws LoadError {
    this(name, source, new CachedScriptCompilation(new SimpleScriptCompilation(0)), loadPath, globalFiles);
  }


  /**
   * Load a new script context from a source, found with a locator,
   * loading globalFiles. 'hegemon/core' is loaded by default.
   *
   * For each file in globalFiles, a module will be loaded and a variable with the name of the globalFile's basename
   * will be injected into the environment. For example, passing a name to globalFiles like "foo/bar/baz" will result
   * in `baz` being made available in the script, just as `let baz = core.load('foo/bar/baz');` had been written.
   *
   * @param name - The name of the script.
   * @param source - The source code to be run.
   * @param scriptCompilation - The compilation strategy to use.
   * @param loadPath - How to find any files loaded.
   * @param globalFiles - Files to load to run this source.
   * @throws LoadError when files don't load properly.
   */
  public Script(final String name,
                final String source,
                final ScriptCompilation scriptCompilation,
                final LoadPath loadPath,
                final String... globalFiles) throws LoadError {
    this.loadPath = loadPath;
    this.loaded = Sets.newHashSet();
    this.loading = Sets.newHashSet();
    this.moduleCache = Maps.newHashMap();
    this.scriptCompilation = scriptCompilation;


    Context context = enterContext();
    try {
      this.localScope = createScope(context, true);

      // Put via moduleNameFor and putProperty
      for (String globalFile : globalFiles) {
        String moduleName = moduleNameFor(globalFile);
        ScriptableObject.putProperty(this.localScope, moduleName, load(globalFile));
      }

      cachedEvaluateString(context, source, name, this.localScope);
    } finally {
      exitContext();
    }
  }


  private void cachedEvaluateString(Context context, String code, String filename, Scriptable scope) {
    this.scriptCompilation.compile(context, filename, code).exec(context, scope);
  }


  /**
   * Getter for the local scope object.
   */
  public Scriptable getScope() {
    return this.localScope;
  }


  private void putCoreObjects(Scriptable scope, boolean includeCore) throws LoadError {
    ScriptableObject.putProperty(scope, "log", Context.javaToJS(LOG, scope));
    ScriptableObject.putProperty(scope, "hegemon", Context.javaToJS(this, scope));
    if (includeCore) {
      ScriptableObject.putProperty(scope, "core", Context.javaToJS(load("hegemon/core"), scope));
    }
  }

  /**
   * Load the script located with the Script's loadPath with the given filename.
   * If a circular dependency is detected, a RuntimeException will be thrown.
   * @param scriptName - the name of the script to load (sans .js).
   * @throws LoadError when unable to load the associated resource.
   */
  public synchronized Object load(final String scriptName) throws LoadError {
    if (this.loading.contains(scriptName)) {
      throw new RuntimeException("Circular dependency when loading: " + scriptName);
    }
    // if we've already loaded it, return it
    if (this.loaded.contains(scriptName)) {
      return this.moduleCache.get(scriptName);
    }
    this.loading.add(scriptName);

    String filename = scriptName + ".js";
    String moduleName = moduleNameFor(scriptName);
    Context context = enterContext();
    try {
      Scriptable newScope = createScope(context, !"hegemon/core".equals(scriptName));

      String code = this.loadPath.load(filename);

      cachedEvaluateString(context, code, filename, newScope);
      try {
        Object preWrap = ScriptableObject.getProperty(newScope, moduleName);
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
      this.loading.remove(scriptName);
      this.loaded.add(scriptName);
      exitContext();
    }
  }


  private Scriptable createScope(Context context, boolean includeCore) throws LoadError {
    Scriptable newScope = context.newObject(PARENT_SCOPE);
    newScope.setParentScope(null);
    newScope.setPrototype(PARENT_SCOPE);
    putCoreObjects(newScope, includeCore);
    return newScope;
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


  private Object[] jsValues(Object[] values) {
    Object[] jsValues = new Object[values.length];
    for (int i = 0; i < values.length; i++) {
      jsValues[i] = Context.javaToJS(values[i], this.localScope);
    }
    return jsValues;
  }


  /**
   * Run the given function by name in the current context.
   * @param functionReference - the name of the function to run.
   * @param values - the arguments passed to the function.
   * @return the result of the function call.
   */
  public Object run(final String functionReference, final Object... values) {
    // Create a local copy of the bindings so we can multi-thread.
    Context context = enterContext();
    try {
      Scriptable object = this.localScope;
      Iterator<String> parts = Splitter.on('.').split(functionReference).iterator();
      while (parts.hasNext()) {
        String property = parts.next();
        if (parts.hasNext()) {
          object = (Scriptable) ScriptableObject.getProperty(object, property);
        } else {

          return unwrap(ScriptableObject.callMethod(context, object, property, jsValues(values)));
        }
      }
      throw new IllegalArgumentException("functionName is empty");
    } finally {
      exitContext();
    }
  }


  /**
   * Call a method on an object in the current context.
   * @param object - the receiver of the call.
   * @param property - the property we're calling.
   * @param values - arguments to the method.
   * @return the result of the method call.
   */
  public Object call(final Object object, final String property, final Object... values) {
    // Create a local copy of the bindings so we can multi-thread.
    Context context = enterContext();
    try {
      return unwrap(ScriptableObject.callMethod(context, (Scriptable) object, property, jsValues(values)));
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

