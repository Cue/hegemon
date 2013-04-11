package com.cueup.hegemon;

import org.mozilla.javascript.Context;

/**
 *
 */
public class ContextFactory extends org.mozilla.javascript.ContextFactory {

  private final ScriptExecutionObserver executionObserver;

  public ContextFactory(ScriptExecutionObserver executionObserver) {
    this.executionObserver = executionObserver;
  }

  protected void observeInstructionCount(Context cx, int instructionCount) {
    this.executionObserver.tick();
  }

}
