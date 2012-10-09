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

load('common');

Assert = org.junit.Assert;


function collectTests(list) {
  for each(var name in getKeys(this)) {
    if (name.substring(0, 4) == 'test') {
      list.add(name);
    }
  }
}


var javaTest;


function setTestInstance(instance) {
  javaTest = instance;
}


function getTestInstance() {
  return javaTest;
}
