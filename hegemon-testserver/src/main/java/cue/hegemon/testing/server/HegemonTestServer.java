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

package cue.hegemon.testing.server;

import cue.hegemon.testing.HegemonRunner;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.runner.notification.RunNotifier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Test server for Hegemon.
 */
public class HegemonTestServer extends AbstractHandler {

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    if ("/favicon.ico".equals(target)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      baseRequest.setHandled(true);
      return;
    }

    if (target.startsWith("/_file/")) {
      String[] parts = target.substring(7).split(":");
      String filename = parts[0];
      int lineNumber = Integer.parseInt(parts[1]);
      Runtime.getRuntime().exec(new String[]{
        "/Applications/IntelliJ IDEA 10.app/Contents/MacOS/idea",
        "--line",
        "" + lineNumber,
        "/var/greplin/src/java/hydra/src/main/webapp/" + filename,
      });
      Runtime.getRuntime().exec("open -b com.jetbrains.intellij");

      response.setContentType("text/html;charset=utf-8");
      response.getWriter().print("ok");
      response.setStatus(HttpServletResponse.SC_OK);
      baseRequest.setHandled(true);
      return;
    }

    try {
      Class c = Class.forName(target.substring(1), true, getClass().getClassLoader());
      HegemonRunner runner = new HegemonRunner(c, baseRequest.getQueryString());
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
      response.setContentType("text/plain;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      baseRequest.setHandled(true);
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
