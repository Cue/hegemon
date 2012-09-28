/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
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
