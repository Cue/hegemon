/*
 * Copyright 2012 the hegemon authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cueup.hegemon;

import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptException;
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

