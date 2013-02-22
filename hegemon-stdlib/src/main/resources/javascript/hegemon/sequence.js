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
 * Calls fn(item) for each item in obj. Handles java arrays vs iteratables properly.
 * @param obj
 * @param fn
 */
sequence.forEach = function(obj, fn) {
  if (obj) {
    if ('length' in obj) {
      for (var i = 0; i < obj.length; i++) {
        fn(obj[i]);
      }
    } else {
      var it = obj.iterator();
      while (it.hasNext()) {
        fn(it.next());
      }
    }

  }
};

/**
 * Calls fn(o) for each o in obj and returns true if all return values are truthy.
 * @param obj - the object to iterate over.
 * @param fn - the function to apply.
 * @return {boolean} true if all values are truthy, else false.
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
 * @param obj - the item to iterate over.
 * @param fn - the function to apply.
 * @return {Array} the results.
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
 * @param obj - the object to iterate over.
 * @param fn - the function to apply.
 * @return {Array} each element in obj for which fn(element) is truthy.
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
 * @param iterable - the item to iterate over.
 * @return {Array} the iterable as an Array.
 */
sequence.toArray = function(iterable) {
  return sequence.map(iterable, function(e) {
    return e;
  });
};


// TODO(kev): Change to forEach usage. We're going to iterate all the way through (for map) anyway.
/**
 * Determines if the given iterable contains the given item.
 * @param iterable the item to iterate over.
 * @param item the item to look for.
 * @return {boolean} whether the item is found.
 */
sequence.contains = function(iterable, item) {
  for each(let each in sequence.toArray(iterable)) {
    if (each == item) {
      return true;
    }
  }
  return false;
};


// Currently actually a find + map?
/**
 * Finds the first item in the iterable where the given function returns true.
 * @param obj the item to iterate over.
 * @param fn the predicate.
 * @return {*} the first matching item found.
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

