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

package com.cueup.hegemon.testing.server;

import java.io.PrintWriter;

/**
 * Class used to include custom test output.
 */
public interface CustomTestOutput {

  /**
   * Called when a test starts.
   * @param output the output stream
   * @throws Exception on error
   */
  void testStarted(PrintWriter output) throws Exception;

  /**
   * Called when a test fails.
   * @param output the output stream
   * @throws Exception on error
   */
  void testFailure(PrintWriter output) throws Exception;

  /**
   * Called when a test succeeds.
   * @param output the output stream
   * @throws Exception on error
   */
  void testSuccess(PrintWriter output) throws Exception;

  /**
   * Called when a test is ignored.
   * @param output the output stream
   * @throws Exception on error
   */
  void testIgnored(PrintWriter output) throws Exception;

}
