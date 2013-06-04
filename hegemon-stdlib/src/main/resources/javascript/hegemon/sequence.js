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


let sequence = {};


/**
 * @typedef {Array|Arguments|{length: number}|java.lang.Iterable}
 */
sequence.SequenceType;


/**
 * Calls fn(item) for each item in obj. Handles java arrays vs iteratables properly.
 * @param {Array.<T>|sequence.SequenceType} obj
 * @param {function(T)} fn The function to call on each item.
 * @template T
 */
sequence.forEach = function(obj, fn) {
  if (obj) {
    if ('length' in obj) {
      for (var i = 0; i < obj.length; i++) {
        fn(obj[i]);
      }
    } else {
      var it = (/** @type java.util.Iterator */ obj.iterator());
      while (it.hasNext()) {
        fn(it.next());
      }
    }
  }
};

/**
 * Calls fn(o) for each o in obj and returns true if all return values are truthy.
 * This is not lazy -- the function will be called for all items in obj even after it returns false for one.
 * @param {Array.<T>|sequence.SequenceType} obj The object to iterate over.
 * @param {function(T)} fn The function to apply.
 * @return {boolean} Whether all return values were truthy.
 * @template T
 */
sequence.forAll = function(obj, fn) {
  let allTrue = true;

  sequence.forEach(obj, function(o) {
    if (! fn(o)) {
      allTrue = false;
    }
  });

  return allTrue;
};


/**
 * Returns an array containing the result of applying fn to each o in object.
 * @param {Array.<T>|sequence.SequenceType} obj The object to iterate over.
 * @param {function(T): ?} fn The function to apply.
 * @return {Array} The results.
 * @template T
 */
sequence.map = function(obj, fn) {
  let result = [];
  sequence.forEach(obj, function(el) {
    result.push(fn(el));
  });
  return result;
};


/**
 * Returns the elements in obj for which fn(element) is truthy.
 * @param {Array.<T>|sequence.SequenceType} obj The object to iterate over.
 * @param {function(T)} fn The function to apply.
 * @return {Array} The items in obj for which fn returned a truthy value.
 * @template T
 */
sequence.filter = function(obj, fn) {
  let result = [];

  sequence.forEach(obj, function(el) {
    if (fn(el)) {
      result.push(el);
    }
  });

  return result;
};


/**
 * Coerces any iterable to an Array.
 * @param {Array.<T>|sequence.SequenceType} iterable The item to iterate over.
 * @return {Array.<T>} The iterable as an Array.
 * @template T
 */
sequence.toArray = function(iterable) {
  let result = [];
  sequence.forEach(iterable, function(x) {
    result.push(x);
  });
  return result;
};


// TODO(kev): Change to forEach usage. We're going to iterate all the way through (for map) anyway.
/**
 * Determines if the given iterable contains the given item.
 * @param {sequence.SequenceType} iterable The item to iterate over.
 * @param {*} item The item to look for.
 * @return {boolean} Whether the item is found.
 */
sequence.contains = function(iterable, item) {
  for each(let each in sequence.toArray(iterable)) {
    // TODO(david): this be a strict ("===") equality check. (need to make sure that changing that won't break anything
    // first, though)
    if (each == item) {
      return true;
    }
  }
  return false;
};


// Currently actually a find + map?
/**
 * Finds the first item in the iterable where the given function returns a truthy value.
 * @param {Array.<T>|sequence.SequenceType} obj the item to iterate over.
 * @param {function(T)} fn The predicate.
 * @return {T?} The first matching item found, or null if there was no result.
 * @template T
 */
sequence.findFirst = function(obj, fn) {
  if (obj) {
    if ('length' in obj) {
      for (let i = 0; i < obj.length; i++) {
        let result = fn(obj[i]);
        if (result) {
          return result;
        }
      }
    } else {
      let it = obj.iterator();
      while (it.hasNext()) {
        let result = fn(it.next());
        if (result) {
          return result;
        }
      }
    }
  }

  return null;
};

/**
 * Appropriated from Google's Closure googe.array.zip
 *
 * Creates a new array for which the element at position i is an array of the
 * ith element of the provided arrays.  The returned array will only be as long
 * as the shortest array provided; additional values are ignored.  For example,
 * the result of zipping [1, 2] and [3, 4, 5] is [[1,3], [2, 4]].
 *
 * This is similar to the zip() function in Python.  See {@link http://docs.python.org/library/functions.html#zip}
 *
 * @param {...} var_args Arrays to be combined.
 * @return {!Array.<!Array>} A new array of arrays created from provided arrays.
 */
sequence.zip = function(var_args) {
  if (!arguments.length) {
    return [];
  }
  var result = [];
  for (var i = 0; true; i++) {
    var value = [];
    for (var j = 0; j < arguments.length; j++) {
      var arr = arguments[j];
      // If i is larger than the array length, this is the shortest array.
      if (i >= arr.length) {
        return result;
      }
      value.push(arr[i]);
    }
    result.push(value);
  }
};
