package com.cueup.hegemon.compilation;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

/**
 * Script compilation that just always compiles the script.
 */
public class SimpleScriptCompilation implements ScriptCompilation {

  private final int optimizationLevel;


  public SimpleScriptCompilation(int optimizationLevel) {
    this.optimizationLevel = optimizationLevel;
  }


  @Override
  public Script compile(Context c, String name, String source) {
    int oldLevel = c.getOptimizationLevel();
    c.setOptimizationLevel(this.optimizationLevel);
    Script result = c.compileString(source, name, 1, null);
    c.setOptimizationLevel(oldLevel);
    return result;
  }

}
