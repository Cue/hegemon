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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

class ResponseListener extends RunListener {

  private final PrintWriter output;

  private final Map<String, Throwable> failed;

  private final Set<String> ignored;


  ResponseListener(HttpServletResponse response) throws IOException {
    this.output = response.getWriter();
    this.failed = Maps.newHashMap();
    this.ignored = Sets.newHashSet();
  }


  @Override
  public void testStarted(Description description) throws Exception {
    this.output.println(
        "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\"></script>");
    this.output.println(
        "<script>function openFile(f) { $.ajax('/_file/' + f) }</script>");
    this.output.println(
        "<a href=\"?" + description.getMethodName() + "\">" + description.getDisplayName() + "</a>...");
    this.output.flush();
  }


  public static String linkFiles(String html) {
    Pattern p = Pattern.compile("\\(([\\w/]+\\.js:\\d+)\\)");
    return p.matcher(html).replaceAll("(<a href=\"javascript:openFile('$1')\">$1</a>)");
  }


  @Override
  public void testFinished(Description description) throws Exception {
    if (this.ignored.contains(description.getDisplayName())) {
      this.output.println("<span class=\"ignored\">IGNORED</span>");
    } else if (this.failed.containsKey(description.getDisplayName())) {
      this.output.println("<span class=\"fail\">FAILED</span>");
      this.output.print("<pre>");
      this.output.print(
          linkFiles(
              StringEscapeUtils.escapeHtml(
                  ExceptionUtils.getStackTrace(this.failed.get(description.getDisplayName())))));
      this.output.print("</pre>");
    } else {
      this.output.println("<span class=\"ok\">OK</span>");
    }
    this.output.println("<br>");
    this.output.flush();
  }


  @Override
  public void testFailure(Failure failure) throws Exception {
    this.failed.put(failure.getDescription().getDisplayName(), failure.getException());
  }


  @Override
  public void testAssumptionFailure(Failure failure) {
    this.failed.put(failure.getDescription().getDisplayName(), failure.getException());
  }


  @Override
  public void testIgnored(Description description) throws Exception {
    this.ignored.add(description.getDisplayName());
  }
}
