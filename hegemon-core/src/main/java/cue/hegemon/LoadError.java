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


package cue.hegemon;

/**
 * An error for when Hegemon can't find the requested file.
 */
public class LoadError extends Exception {
  /**
   * Basic constructor.
   */
  public LoadError() {
    super();
  }


  /**
   * Creates a LoadError which wraps a lower level Throwable.
   * @param cause the Throwable to wrap.
   */
  public LoadError(Throwable cause) {
    super(cause);
  }


  /**
   * Creates a LoadError with a custom message.
   * @param message the message to attach to the exception.
   */
  public LoadError(String message) {
    super(message);
  }


  /**
   * Creates a LoadError wrapping a Throwable with a custom message.
   * @param message the message to attach.
   * @param cause the Throwable to wrap.
   */
  public LoadError(String message, Throwable cause) {
    super(message, cause);
  }
}
