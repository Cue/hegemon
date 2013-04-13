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
import org.mozilla.javascript.EcmaError;

import javax.script.ScriptException;
import java.util.Random;

/**
 * Tests for the Script class.
 */
public class ScriptTest {
  @Test
  public void functionsShouldReturnJavaNativeTypes() throws ScriptException, LoadError {
    Script s = new Script("test", "function add(a, b) { return a + b }");
    Assert.assertEquals(106.0, s.run("add", 6, 100));
    Assert.assertEquals("ab", s.run("add", "a", "b"));
  }


  @Test
  public void concurrentRunsShouldNotEffectOneAnother() throws ScriptException, InterruptedException, LoadError {
    final Script s = new Script("test", "function add(a, b) { return a + b }");
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
  public void loadViaGlobalFilesImportsModuleSymbol() throws Exception {
    final Script s = new Script("test", "function tester() { return test.me(); }", LoadPaths.defaultPath(), "hegemon/test");
    Assert.assertEquals("here", s.run("tester"));
  }


  @Test
  public void loadViaInternalFunctionReturnsModule() throws ScriptException, LoadError {
    final Script s = new Script("test", "let test = core.load('hegemon/test'); function tester() { return test.me(); }");
    Assert.assertEquals("here", s.run("tester"));
  }

  @Test
  public void loadViaInternalFunctionDoesntPolluteGlobals() throws Exception {
    try {
      final Script s = new Script(
          "test", "let testImport = core.load('hegemon/testImport'); function tester() { return FOO_BAR; }");
      s.run("tester");
      Assert.fail();
    } catch (EcmaError e) {
      if (!e.getMessage().startsWith("ReferenceError: \"FOO_BAR\"")) {
        throw e;
      }
    }
  }

  @Test(expected = RuntimeException.class)
  public void circularDependencyThrowsException() throws Exception {
    new Script("test", "", LoadPaths.defaultPath(), "hegemon/testCircleA");
  }


  static class Observer implements ScriptExecutionObserver {
    public int tickCounter = 0;

    public void tick() {
      this.tickCounter++;
    }
  }


  @Test
  public void executionObserverIsCalledWhenUsingCodeGen() throws Exception {
    Observer observer = new Observer();
    ScriptOptions options = ScriptOptions.builder()
                                         .setOptimizationLevel(1)
                                         .observeExecution(1, observer)
                                         .build();
    ScriptCache cache = new ScriptCache(LoadPaths.defaultPath(), options);
    Script s = new Script(cache, "test", "function tester() { var total=0; for(var i=0; i<1024; i++) { total++; } return total; }");
    Double result = (Double) s.run("tester");
    Assert.assertEquals(result.doubleValue(), 1024.0, 0.0);
    Assert.assertTrue("About 3000 instructions should have been executed", observer.tickCounter > 3000);
  }


  @Test
  public void executionObserverIsCalledWhenUsingInterpreter() throws Exception {
    Observer observer = new Observer();
    ScriptOptions options = ScriptOptions.builder()
                                         .setOptimizationLevel(-1)
                                         .observeExecution(1, observer)
                                         .build();
    ScriptCache cache = new ScriptCache(LoadPaths.defaultPath(), options);
    Script s = new Script(cache, "test", "function tester() { var total=0; for(var i=0; i<1024; i++) { total++; } return total; }");
    Double result = (Double) s.run("tester");
    Assert.assertEquals(result.doubleValue(), 1024.0, 0.0);
    Assert.assertTrue("About 1024 instructions should have been executed", observer.tickCounter > 1000);
  }


}

