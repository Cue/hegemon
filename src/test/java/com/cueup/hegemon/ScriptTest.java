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

  private static final ScriptLocator LOCATOR = new ResourceScriptLocator("hegemon");


  @Test
  public void testBasics() throws ScriptException, IOException {
    Script s = new Script("function add(a, b) { return a + b }", LOCATOR);
    Assert.assertEquals(106.0, s.run("add", 6, 100));
    Assert.assertEquals("ab", s.run("add", "a", "b"));
  }


  @Test
  public void testConcurrent() throws ScriptException, InterruptedException, IOException {
    final Script s = new Script("function add(a, b) { return a + b }", LOCATOR);
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
  public void testGlobals() throws ScriptException, IOException {
    final Script s = new Script("", LOCATOR, "core", "test");
    Assert.assertEquals("here", s.run("testMe"));
  }


  @Test
  public void testImports() throws ScriptException, IOException {
    final Script s = new Script("", LOCATOR, "core", "test");
    Assert.assertEquals(100, s.run("testImports"));
  }

}

