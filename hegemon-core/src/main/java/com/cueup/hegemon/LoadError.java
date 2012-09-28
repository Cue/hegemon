/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */


package com.cueup.hegemon;

public class LoadError extends Exception {
  public LoadError() {
    super();
  }


  public LoadError(Throwable cause) {
    super(cause);
  }


  public LoadError(String message) {
    super(message);
  }


  public LoadError(String message, Throwable cause) {
    super(message, cause);
  }
}
