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

package cue.hegemon.guice;

import cue.hegemon.LoadError;
import cue.hegemon.LoadPath;
import cue.hegemon.Script;
import cue.hegemon.ScriptCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.script.ScriptException;


/**
 * A ScriptCache suitable for injection.
 */
@Singleton
public class InjectableScriptCache extends ScriptCache {

  /**
   * Simple constructor.
   */
  @Inject
  public InjectableScriptCache(final LoadPath loadPath) {
    super(loadPath);
  }


  @Override
  public Script get(String script, boolean reload) throws LoadError, ScriptException {
    return super.get(script, reload);
  }
}
