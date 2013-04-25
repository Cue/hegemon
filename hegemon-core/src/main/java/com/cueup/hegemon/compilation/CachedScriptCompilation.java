package com.cueup.hegemon.compilation;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

import java.lang.ref.WeakReference;

/**
 * In-memory cache for script compilation.
 */
public class CachedScriptCompilation implements ScriptCompilation {

  /**
   * We cache compiled scripts to save time during initialization and tests.
   */
  private class CompilationKey {

    private final WeakReference<Context> c;
    private final String source;
    private final String name;
    private final int hash;

    public CompilationKey(Context c, String source, String name) {
      this.c = new WeakReference<Context>(c);
      this.source = source;
      this.name = name;
      this.hash = Objects.hashCode(System.identityHashCode(c), source, name);
    }


    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      CompilationKey other = (CompilationKey) o;

      return this.hash == other.hash
          && this.c.get() == other.c.get() && this.c.get() != null
          && this.name.equals(other.name)
          && this.source.equals(other.source);
    }


    @Override
    public int hashCode() {
      return this.hash;
    }
  }

  private final LoadingCache<CompilationKey, Script> compilationCache;


  public CachedScriptCompilation(final ScriptCompilation compilation) {
    this.compilationCache = CacheBuilder.newBuilder()
        .maximumSize(500)
        .build(new CacheLoader<CompilationKey, Script>() {
          @Override
          public org.mozilla.javascript.Script load(CompilationKey key) throws Exception {
            return compilation.compile(key.c.get(), key.name, key.source);
          }
        });
  }


  @Override
  public Script compile(Context c, String name, String source) {
    return this.compilationCache.getUnchecked(new CompilationKey(c, source, name));
  }

}
