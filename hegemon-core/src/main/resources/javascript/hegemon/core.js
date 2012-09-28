/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

function load(name) {
  hegemon.load(name);
}

// Returns the contents of a file as a string
function slurp(name) {
  return hegemon.read(name);
}
