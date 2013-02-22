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

let sequence = core.load('hegemon/sequence');
let Assert = core.load('hegemon/unittest').Assert;

function testForEachWithJSObject() {
  let results = [];
  let iterable = [1, 2, 3];
  sequence.forEach(iterable, function(val) {
    results.push(val);
  });
  Assert.assertEquals(results, iterable);
}

function testForEachWithJavaObject() {
  let results = [];
  let list = java.util.LinkedList();
  list.addAll(['a', 'b', 'c']);
  sequence.forEach(list, function(val) {
    results.push(val);
  });
  Assert.assertEquals(['a', 'b', 'c'], results);
}

function testForEachWithJavaArray() {
  let array = com.cueup.hegemon.stdlib.JavaJsTest.arrayOf('a', 'b', 'c');
  let results = [];
  sequence.forEach(array, function(val) {
    results.push(val);
  });
  Assert.assertEquals(['a', 'b', 'c'], results);
}
