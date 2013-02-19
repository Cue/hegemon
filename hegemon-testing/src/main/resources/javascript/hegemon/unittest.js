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

let inspect = core.load('hegemon/inspect');

let unittest = {};

unittest.Assert = org.junit.Assert;

/**
 * Returns all keys in a scope that start with 'test' and places them in list.
 * @param scope {Scriptable} a Rhino Scriptable suitable as a scope.
 * @param list {java.util.List} the list to add to.
 */
unittest.collectTests = function(scope, list) {
  for each(var name in inspect.getKeys(scope)) {
    if (name.substring(0, 4) == 'test') {
      list.add(name);
    }
  }
};

var javaTest;

/**
 * Stores the test instance for use later.
 * @param instance - the instance to store.
 */
unittest.setTestInstance = function(instance) {
  javaTest = instance;
};

/**
 * Getter for the test instance.
 * @return the javaTest instance.
 */
unittest.getTestInstance = function() {
  return javaTest;
};
