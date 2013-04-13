package com.cueup.hegemon;

import org.mozilla.javascript.Context;

/**
 * Encapsulates script execution options.
 */
public class ScriptOptions {

  private final int version;
  private final int optimizationLevel;
  private final int instructionObserverThreshold;
  private final ScriptExecutionObserver executionObserver;

  public static final ScriptOptions DEFAULT_OPTIONS = ScriptOptions.builder()
      .setVersion(Context.VERSION_1_8)
      .setOptimizationLevel(0)
      .build();


  private ScriptOptions(int version,
                        int optimizationLevel,
                        int instructionObserverThreshold,
                        ScriptExecutionObserver executionObserver) {
    this.version = version;
    this.optimizationLevel = optimizationLevel;
    this.instructionObserverThreshold = instructionObserverThreshold;
    this.executionObserver = executionObserver;
  }


  public int getVersion() {
    return version;
  }

  public int getOptimizationLevel() {
    return optimizationLevel;
  }

  public int getInstructionObserverThreshold() {
    return this.instructionObserverThreshold;
  }

  public ScriptExecutionObserver getExecutionObserver() {
    return executionObserver;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ScriptOptions that = (ScriptOptions) o;

    if (this.instructionObserverThreshold != that.instructionObserverThreshold) return false;
    if (this.optimizationLevel != that.optimizationLevel) return false;
    if (this.version != that.version) return false;
    if (this.executionObserver != null ? !this.executionObserver.equals(that.executionObserver) : that.executionObserver != null)
      return false;

    return true;
  }


  @Override
  public int hashCode() {
    int result = this.version;
    result = 31 * result + this.optimizationLevel;
    result = 31 * result + this.instructionObserverThreshold;
    result = 31 * result + (this.executionObserver != null ? this.executionObserver.hashCode() : 0);
    return result;
  }


  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int version;
    private int optimizationLevel;
    private int instructionObserverThreshold;
    private ScriptExecutionObserver executionObserver;

    public Builder setVersion(int version) {
      this.version = version;
      return this;
    }

    public Builder setOptimizationLevel(int optimizationLevel) {
      this.optimizationLevel = optimizationLevel;
      return this;
    }

    public Builder observeExecution(int instructionObserverThreshold,
                                    ScriptExecutionObserver executionObserver) {
      this.instructionObserverThreshold = instructionObserverThreshold;
      this.executionObserver = executionObserver;
      return this;
    }

    public ScriptOptions build() {
      return new ScriptOptions(this.version, this.optimizationLevel,
                               this.instructionObserverThreshold, this.executionObserver);
    }

  }

}
