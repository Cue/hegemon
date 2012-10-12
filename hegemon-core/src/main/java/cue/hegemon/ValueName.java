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
 * A value identifier. Used to give names to static values.
 */
public class ValueName {
  private final String namespace;
  private final String name;


  /**
   * Simple constructor.
   * @param namespace to define the name in.
   * @param name of the value.
   */
  public ValueName(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }
}
