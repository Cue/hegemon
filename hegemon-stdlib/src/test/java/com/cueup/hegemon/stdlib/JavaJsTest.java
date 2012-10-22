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

package com.cueup.hegemon.stdlib;

import com.cueup.hegemon.testing.HegemonRunner;
import org.junit.runner.RunWith;

// lint: disable=HideUtilityClassConstructorCheck next 20 lines
/**
 * Tests for 'hegemon/java'.
 */
@RunWith(HegemonRunner.class)
@HegemonRunner.TestScript(filename = "hegemon/javaTest")
public class JavaJsTest {
  public static Object[] arrayOf(Object ... args) {
    return args;
  }
}
