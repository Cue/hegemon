/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.Random;

/**
 * Tests for the Script class.
 */
public class ScriptTest {

  private static final LoadPath LOAD_PATH = new LoadPath(new ResourceScriptLocator(ScriptTest.class, "/javascript"));


  @Test
  public void testBasics() throws ScriptException, LoadError {
    Script s = new Script("function add(a, b) { return a + b }", LOAD_PATH);
    Assert.assertEquals(106.0, s.run("add", 6, 100));
    Assert.assertEquals("ab", s.run("add", "a", "b"));
  }


  @Test
  public void testConcurrent() throws ScriptException, InterruptedException, LoadError {
    final Script s = new Script("function add(a, b) { return a + b }", LOAD_PATH);
    final Random random = new Random();
    TestUtils.runConcurrent(10, new Runnable() {
      @Override
      public void run() {
        double a = random.nextDouble();
        Integer b = random.nextInt(100);
        Assert.assertEquals(a + b, s.run("add", a, b));
      }
    });
  }


  @Test
  public void testGlobals() throws ScriptException, LoadError {
    final Script s = new Script("", LOAD_PATH, "hegemon/core", "hegemon/test");
    Assert.assertEquals("here", s.run("testMe"));
  }


  @Test
  public void testImports() throws ScriptException, LoadError {
    final Script s = new Script("", LOAD_PATH, "hegemon/core", "hegemon/test");
    Assert.assertEquals(100, s.run("testImports"));
  }

}

