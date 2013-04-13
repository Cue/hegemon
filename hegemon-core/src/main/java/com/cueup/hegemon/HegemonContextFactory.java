package com.cueup.hegemon;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 *
 */
public class HegemonContextFactory extends org.mozilla.javascript.ContextFactory {

  private final ScriptOptions scriptOptions;

  /**
   * Create and install a hegemon context factory with the given options as the global rhino context factory.
   * @param scriptOptions
   */
  public HegemonContextFactory(ScriptOptions scriptOptions) {
    this.scriptOptions = scriptOptions;
  }

  protected void observeInstructionCount(Context cx, int instructionCount) {
    ScriptExecutionObserver observer = this.scriptOptions.getExecutionObserver();
    if (observer != null) {
      this.scriptOptions.getExecutionObserver().tick();
    }
  }

  protected Context makeContext() {
    Context context = super.makeContext();
    context.setLanguageVersion(this.scriptOptions.getVersion());
    context.setOptimizationLevel(this.scriptOptions.getOptimizationLevel());
    if (this.scriptOptions.getExecutionObserver() != null) {
      context.setInstructionObserverThreshold(this.scriptOptions.getInstructionObserverThreshold());
    }

    return context;
  }

}
