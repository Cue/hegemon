/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

// A test for Script loading.

load('hegemon/testImport');

function testMe() {
  return 'here';
}

function testImports() {
  return importedVar;
}
