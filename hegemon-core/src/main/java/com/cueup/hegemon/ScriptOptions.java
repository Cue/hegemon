package com.cueup.hegemon;

import org.mozilla.javascript.Context;

import java.util.concurrent.TimeUnit;

/**
 * Encapsulates script execution options.
 */
public class ScriptOptions {

  private final int version;
  private final int optimizationLevel;
  private final int instructionObserverThreshold;
  private final ScriptExecutionObserver executionObserver;

  public static final int VERSION_1_8 = Context.VERSION_1_8;

  public static final ScriptOptions DEFAULT_OPTIONS = new ScriptOptions(VERSION_1_8, 0, 0, null);


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

  public static class ExecutionTimeObserver implements ScriptExecutionObserver {

    private final long upperBound;

    public ExecutionTimeObserver(long maxTime, TimeUnit unit) {
      this.upperBound = System.currentTimeMillis() + unit.toMillis(maxTime);
    }

    public void tick() {
      if (System.currentTimeMillis() > this.upperBound) {
        throw new Error("Maximum script execution time reached");
      }
    }

  }

  public static class Builder {

    private int version = VERSION_1_8;
    private int optimizationLevel = 0;
    private int instructionObserverThreshold = 0;
    private ScriptExecutionObserver executionObserver = null;


    /**
     * Set the javascript language version.
     *
     * @param version the version. VERSION_1_8 by default.
     * @return
     */
    public Builder setVersion(int version) {
      this.version = version;
      return this;
    }


    /**
     * Set the optimization level.
     * -1: use the interpreter
     * 0: codegen without optimizations
     * 1: codegen with standard optimizations
     * Other levels are currently undefined.
     *
     * @param optimizationLevel the optimization level.
     * @return
     */
    public Builder setOptimizationLevel(int optimizationLevel) {
      this.optimizationLevel = optimizationLevel;
      return this;
    }


    /**
     * Observe script execution: executionObserver.tick() will be called approximately every
     * instructionObserverThreshold statements (if using the interpreter) or instructions (with codegen).
     * @param instructionObserverThreshold
     * @param executionObserver
     * @return
     */
    public Builder observeExecution(int instructionObserverThreshold,
                                    ScriptExecutionObserver executionObserver) {
      this.instructionObserverThreshold = instructionObserverThreshold;
      this.executionObserver = executionObserver;
      return this;
    }


    /**
     * Kill scripts that have been running for too long by throwing an Error.
     * The same goal can also by achieved by manually setting instructionObserverThreshold and executionObserver,
     * and this is merely a helper with sensible defaults.
     * @param duration
     * @param unit
     * @return
     */
    public Builder maximumExecutionTime(long duration, TimeUnit unit) {
      if (this.optimizationLevel < 0) {
        // In interpreted mode an instruction is basically a statement
        this.instructionObserverThreshold = 1000;
      } else {
        // In codegen mode instructions approximate bytecode instructions
        this.instructionObserverThreshold = 10000;
      }
      this.executionObserver = new ExecutionTimeObserver(duration, unit);
      return this;
    }


    /**
     * Finish building ScriptOptions.
     *
     * @return a ScriptOptions instance.
     */
    public ScriptOptions build() {

      if (this.instructionObserverThreshold > 0) {
        assert this.executionObserver != null;
      }

      if (this.executionObserver != null) {
        assert this.instructionObserverThreshold > 0;
      }

      return new ScriptOptions(this.version, this.optimizationLevel,
                               this.instructionObserverThreshold, this.executionObserver);
    }

  }

}
