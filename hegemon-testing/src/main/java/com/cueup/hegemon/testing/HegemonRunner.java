/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon.testing;

import com.cueup.hegemon.LoadError;
import com.cueup.hegemon.LoadPath;
import com.cueup.hegemon.ResourceScriptLocator;
import com.cueup.hegemon.Script;
import com.cueup.hegemon.ScriptLocator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Base class for Hegemon tests.
 */
public class HegemonRunner extends ParentRunner<String> {

  /**
   * The Script annotation specifies the scrpt to be run when a class
   * annotated with <code>@RunWith(HegemonRunner.class)</code> is run.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public @interface TestScript {
    /**
     * @return the script to be run
     */
    String filename();
  }


  /**
   * Runs script test as a JUnit statement.
   */
  private static class RunScriptTest extends Statement {

    private final Script script;

    private final Object instance;

    private final Object[] arguments;

    private final String name;


    public RunScriptTest(Script script, Object instance, Object[] arguments, String name) {
      this.script = script;
      this.instance = instance;
      this.arguments = arguments;
      this.name = name;
    }


    @Override
    public void evaluate() throws Exception { // lint: disable=IllegalThrowsCheck
      Script.enterContext();
      try {
        this.script.run("setTestInstance", this.instance);
        this.script.run(this.name, this.arguments);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      } finally {
        Script.exitContext();
      }
    }

  }


  private final Script testScript;

  private final Object instance;

  // TODO(kevinclark): Nullable annotation? Requires dependency.
  private final String method;


  /**
   * Creates a HegemonRunner to run the given class.
   * @param klass the class to run tests for.
   * @throws InitializationError if the test class is malformed.
   */
  public HegemonRunner(Class<?> klass) throws InitializationError {
    this(klass, null);
  }

  public HegemonRunner(Class<?> klass, String method) throws InitializationError {
    this(klass, method, new LoadPath(new ResourceScriptLocator(HegemonRunner.class, "javascript")));
  }

  /**
   * Creates a HegemonRunner to run the given method of the class.
   * @param klass the class to run tests for.
   * @param method the method to run.
   * @throws InitializationError if the test class is malformed.
   */
  public HegemonRunner(Class<?> klass, String method, LoadPath loadPath) throws InitializationError {
    super(klass);

    this.method = method;

    try {
      this.instance = klass.newInstance();
    } catch (InstantiationException e) {
      throw new InitializationError(e);
    } catch (IllegalAccessException e) {
      throw new InitializationError(e);
    }

    TestScript scriptData = klass.getAnnotation(TestScript.class);
    if (scriptData == null) {
      throw new InitializationError("Hegemon tests must be annotated with @Script");
    }
    try {
      this.testScript = new Script("", loadPath, "hegemon/core", "hegemon/unittest", scriptData.filename());
    } catch (LoadError e) {
      throw new InitializationError(e);
    }
  }


  @Override
  protected List<String> getChildren() {
    if (this.method == null) {
      List<String> testNames = Lists.newArrayList();
      this.testScript.run("collectTests", testNames);
      return testNames;
    } else {
      return ImmutableList.of(this.method);
    }
  }


  @Override
  protected Description describeChild(String child) {
    return Description.createTestDescription(this.getTestClass().getJavaClass(), child);
  }


  @Override
  protected void runChild(String child, RunNotifier notifier) {
    Description d = describeChild(child);
    notifier.fireTestStarted(d);

    Object[] arguments = null;
    try {
      arguments = (Object[]) this.getTestClass().getJavaClass().getMethod("getArguments").invoke(this.instance);
    } catch (IllegalAccessException e) {
      notifier.fireTestFailure(new Failure(d, e));
    } catch (InvocationTargetException e) {
      notifier.fireTestFailure(new Failure(d, e));
    } catch (NoSuchMethodException e) {
      arguments = new Object[0];
    }

    Statement statement = new RunScriptTest(this.testScript, this.instance, arguments, child);

    List<FrameworkMethod> before = getTestClass().getAnnotatedMethods(Before.class);
    if (!before.isEmpty()) {
      statement = new RunBefores(statement, before, this.instance);
    }

    List<FrameworkMethod> after = getTestClass().getAnnotatedMethods(After.class);
    if (!after.isEmpty()) {
      statement = new RunAfters(statement, after, this.instance);
    }

    if (arguments != null) {
      try {
        statement.evaluate();
      } catch (Throwable e) { // lint: disable=IllegalCatchCheck
        notifier.fireTestFailure(new Failure(d, e));
      }
    }

    notifier.fireTestFinished(d);
  }

}

