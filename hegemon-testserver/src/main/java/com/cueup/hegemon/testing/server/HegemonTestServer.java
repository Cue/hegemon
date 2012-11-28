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

package com.cueup.hegemon.testing.server;

import com.cueup.hegemon.LoadPath;
import com.cueup.hegemon.testing.HegemonRunner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.runner.notification.RunNotifier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Test server for Hegemon.
 */
public class HegemonTestServer extends AbstractHandler {

  private final LoadPath loadPath;


  /**
   * Creates a test server loading sources with 'loadPath'.
   */
  public HegemonTestServer(LoadPath loadPath) {
    this.loadPath = loadPath;
  }


  private static class AllClassesList {

    private static final Set<String> TEST_CLASS_NAMES;

    private static final Map<String, String> TEST_CLASS_SHORT_NAMES;

    private static final Map<String, String> JS_NAME_TO_CLASS_NAME;

    private static void addClass(String className) {
      try {
        Class<?> c = Class.forName(className, false, AllClassesList.class.getClassLoader());
        if (c.isAnnotationPresent(HegemonRunner.TestScript.class)) {
          TEST_CLASS_NAMES.add(className);
          TEST_CLASS_SHORT_NAMES.put(c.getSimpleName(), className);
          JS_NAME_TO_CLASS_NAME.put(c.getAnnotation(HegemonRunner.TestScript.class).filename(), className);
        } else {
          System.err.println("No annotation: " + className);
        }
      } catch (ClassNotFoundException e) {
        System.err.println("Could not load: " + className);
      }
    }

    private static void addClassesFromJar(File jarFile) throws IOException {
      JarInputStream is = new JarInputStream(new FileInputStream(jarFile));
      JarEntry entry;
      while ((entry = is.getNextJarEntry()) != null) {
        String name = entry.getName();
        if (name.endsWith("Test.class")) {
          addClass(FilenameUtils.removeExtension(name).replaceAll("/", "."));
        }
      }
    }

    private static void addClassesFromPath(File root) throws IOException {
      Collection files = FileUtils.listFiles(root, new String[]{"class"}, true);
      for (Object file : files) {
        String name = root.toURI().relativize(((File) file).toURI()).getPath();
        if (name.endsWith("Test.class")) {
          addClass(name.substring(0, name.length() - 6).replaceAll("/", "."));
        }
      }
    }

    static {
      TEST_CLASS_NAMES = Sets.newHashSet();
      TEST_CLASS_SHORT_NAMES = Maps.newHashMap();
      JS_NAME_TO_CLASS_NAME = Maps.newHashMap();
      try {
        String classPath = System.getProperty("java.class.path");
        String separator = System.getProperty("path.separator");
        for (String classpathEntry : classPath.split(separator)) {
          System.err.println("Adding class path: " + classpathEntry);
          if (classpathEntry.endsWith(".jar")) {
            addClassesFromJar(new File(classpathEntry));
          } else {
            addClassesFromPath(new File(classpathEntry));
          }
        }

        ClassLoader loader = AllClassesList.class.getClassLoader();
        if (loader instanceof URLClassLoader) {
          for (URL base : ((URLClassLoader) loader).getURLs()) {
            if ("file".equals(base.getProtocol())) {
              System.err.println("Adding class path: " + base);
              if (base.getPath().endsWith(".jar")) {
                addClassesFromJar(new File(base.getPath()));
              } else {
                addClassesFromPath(new File(base.getPath()));
              }
            } else {
              System.err.println("Ignoring class path: " + base);
            }
          }
        }
      } catch (Throwable t) { // lint: disable=IllegalCatchCheck
        t.printStackTrace();
      }
    }
  }


  private static Class loadClass(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }


  private static Class loadClassByReference(String name) {
    Class result = loadClass(name);
    if (result != null) {
      return result;
    }

    List<String> toTry = Lists.newArrayList();
    toTry.add(AllClassesList.TEST_CLASS_SHORT_NAMES.get(name));
    toTry.add(AllClassesList.JS_NAME_TO_CLASS_NAME.get(name));
    for (String option : toTry) {
      if (option != null) {
        result = loadClass(option);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }


  private static void markHandled(Request baseRequest, HttpServletResponse response, int status, String contentType) {
    response.setContentType(contentType + ";charset=utf-8");
    response.setStatus(status);
    baseRequest.setHandled(true);
  }


  /**
   *  The request handler translates an HTTP request to a test class and method to run.
   */
  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    if ("/favicon.ico".equals(target)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      baseRequest.setHandled(true);
      return;
    }

    if (target.length() <= 1) {
      String[] names = AllClassesList.TEST_CLASS_NAMES.toArray(new String[AllClassesList.TEST_CLASS_NAMES.size()]);
      Arrays.sort(names);
      markHandled(baseRequest, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "text/html");
      for (String name : names) {
        response.getWriter().println("<p><a href=\"" + name + "\">" + name + "</a></p>");
      }
      return;
    }

    try {
      Class c = loadClassByReference(target.substring(1));
      if (c == null) {
        markHandled(baseRequest, response, HttpServletResponse.SC_NOT_FOUND, "text/plain");
        response.getWriter().println("Could not find class with name or reference to " + target.substring(1));
        return;
      }

      HegemonRunner runner = new HegemonRunner(c, baseRequest.getQueryString(), this.loadPath);
      RunNotifier notifier = new RunNotifier();
      notifier.addListener(new ResponseListener(response));

      long start = System.currentTimeMillis();

      response.setContentType("text/html;charset=utf-8");
      response.getWriter().print(
          "<style>.ok {color:green} .fail {color:red} .ignore {color:orange} pre {margin-left: 24px}</style>");
      runner.run(notifier);
      response.setStatus(HttpServletResponse.SC_OK);

      response.getWriter().println("<br>Finished in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");

      baseRequest.setHandled(true);

    } catch (Throwable t) { // lint: disable=IllegalCatchCheck
      markHandled(baseRequest, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "text/plain");
      t.printStackTrace(response.getWriter());
    }

  }


  /**
   * Starts an http server on a port that responds to a passed handler.
   */
  public static void run(AbstractHandler handler, int port) throws Exception {
    Server server = new Server(port);
    server.setHandler(handler);

    server.start();
    server.join();
  }

}
