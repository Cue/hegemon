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



/**
 * Returns the item if it's non falsy, else null.
 *
 * Used because round tripping null Java -> JS -> Java loses the type.
 *
 * @param {Object} el - The type that may or may not be there.
 * @return {Object} el if it is truthy, else null.
 */
let nullable = function(el) { return el || null; };


IGNORE_KEYS = {
  'notifyAll': 1,
  'notify': 1,
  'wait': 1,
  'toString': 1,
  'hashCode': 1,
  'equals': 1,
  'getClass': 1,
  'class': 1
};


function objectItems(obj) {
  let result = [];
  for (var key in obj) {
    if (!IGNORE_KEYS[key]) {
      result.push([key, obj[key]]);
    }
  }
  return result;
}



function getKeys(obj) {
  let result = [];
  for (var key in obj) {
    if (!IGNORE_KEYS[key]) {
      result.push(key);
    }
  }
  return result;
}




