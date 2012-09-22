/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.cueup.hegemon;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;

import java.util.List;

/**
 * Utilities for tests.
 */
public class TestUtils {

  private TestUtils() { }


  /**
   * Utility class that collects errors from sub-threads.
   */
  private static class ErrorCollector implements Thread.UncaughtExceptionHandler {

    private final List<String> errors = Lists.newArrayList();

    @Override
    public synchronized void uncaughtException(Thread thread, Throwable throwable) {
      this.errors.add(thread.getName() + ": " + ExceptionUtils.getFullStackTrace(throwable));
    }


    public void assertNoErrors() {
      if (!this.errors.isEmpty()) {
        Assert.fail(Joiner.on("\n\n").join(this.errors));
      }
    }

  }

  public static void runConcurrent(int count, Runnable r) throws InterruptedException {
    List<Thread> threads = Lists.newArrayList();
    ErrorCollector errorCollector = new ErrorCollector();

    for (int i = 0; i < count; i++) {
      Thread t = new Thread(r);
      t.setName("testThread" + i);
      t.setUncaughtExceptionHandler(errorCollector);
      threads.add(t);

    }

    for (Thread t : threads) {
      t.start();
    }

    for (Thread t : threads) {
      t.join();
    }

    errorCollector.assertNoErrors();
  }

}

