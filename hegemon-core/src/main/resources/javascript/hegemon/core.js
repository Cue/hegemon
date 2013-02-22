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

core = {};

/**
 * Returns the module requested.
 * @param name - the name of the module to load.
 * @return {Object} the named module object.
 */
core.load = function(name) {
  return hegemon.load(name);
};


/**
 * Returns the contents of a file as a string
 * @param name - the name of the module to load.
 * @return {Object} the contents of the module's file.
 */
core.slurp = function(name) {
  return hegemon.read(name);
};


/**
 * Returns the item if it's non falsy, else null.
 *
 * Used because round tripping null Java -> JS -> Java loses the type.
 *
 * @param {Object} el - The type that may or may not be there.
 * @return {Object} el if it is truthy, else null.
 */
core.nullable = function(el) { return el || null; };


