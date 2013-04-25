package com.cueup.hegemon.compilation;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

/**
 * Interface for script compilation.
 */
public interface ScriptCompilation {

  Script compile(Context c, String name, String source);

}
