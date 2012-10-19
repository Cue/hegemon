# Hegemon

hegemon is a set of tools and libraries for running the latest [Rhino](https://developer.mozilla.org/en-US/docs/Rhino)
JavaScript implementation on the JVM.

hegemon currently embeds Rhino 1.7R4, supporting JavaScript 1.8.

For examples in the context of a java project, see the [example application](https://github.com/Cue/hegemon-example).

### hegemon-core

The essentials of hegemon allow you to run JavaScript functions from Java without boilerplate.

```java
Script script = new Script("function foo() { return 3 + 4; }", LoadPath.defaultPath());
script.run("foo"); // returns 7
```

hegemon also allows you to load JavaScript files both from Java, via
varargs in the `Script` constructor:

```java
new Script("function foo() { return definedInUtil(); }", LoadPath.defaultPath(), "util");
```

And also from inside your JavaScript itself with the hegemon provided `load` function:

```java
new Script("load('util'); function foo() { return definedInUtil(); }", LoadPath.defaultPath());
```

Reloading files can be expensive, so hegemon-core ships with a `ScriptCache`.

```java
ScriptCache cache = new ScriptCache(LoadPath.defaultPath());
Script script = cache.get("myScript");
script.run("foo");
// with optional reloading: cache.get("myScript", true);
```

The default load path loads files with a .js extension from the
`resources/javascript` directory. Custom load schemes can be implemented
with the `ScriptLocator` interface.

These simple examples show JavaScript evalution out of context - usage at Cue has largely been via HTTP. For example, the sample
appliction includes a [jersey resource][endpoint] that maps URLs to pre-packaged [javascript files][scripts].
This allows requests to `/script/example` to run code in `resources/javascript/script/example.js`.
[Another resource][customscript] evaluates code that's POST'd to it. This pattern is used primarily to provide a debugging entrypoint.
You can build an interactive console on top of it or bundle code in development and run in against production data
without changing what consumers of the service experience - assuming your eval'd code doesn't do anything nefarios.
Take care to understand the implications of an eval endpoint if you follow this pattern.



[endpoint]: https://github.com/Cue/hegemon-example/blob/master/src/main/java/com/cueup/hegemon/example/ScriptResource.java
[customscript]: https://github.com/Cue/hegemon-example/blob/master/src/main/java/com/cueup/hegemon/example/CustomScriptResource.java
[scripts]: https://github.com/Cue/hegemon-example/tree/master/src/main/resources/javascript/script


Add with maven:

```xml
<dependencies>
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-core</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
```


### hegemon-testing

If you're going to have production code in JavaScript, you're going to
want to be able to write and run tests. `hegemon-testing` provides the
bindings to JUnit so you can write JavaScript tests without the rest of
your project having to care.

Just drop a file in `test/resources/javascript` and a binding Java class
like so:

```java
@RunWith(HegemonRunner.class)
@HegemonRunner.TestScript(filename = "myJsTest") // Maps to test/resources/javascript/myJsTest.js
public class MyJsTest {
}
```

Now any functions prefixed with 'test' in 'myJsTest.js' will be run
along with all other JUnit tests.


In context examples:

* A [binding file](https://github.com/Cue/hegemon-example/blob/master/src/test/java/com/cueup/hegemon/example/ExampleTest.java)
* [Tests in JavaScript](https://github.com/Cue/hegemon-example/blob/master/src/test/resources/javascript/exampleTest.js)


Add with maven:

```xml
<dependencies>
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-testing</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

### hegemon-testserver

The disadvantage of connecting JavaScript unit tests to JUnit is that
you've got to recompile for every test cycle. The `hegemon-testserver`
project speeds up the feedback loop by removing the recompile step when
you're only changing JavaScript. Subclass `HegemonTestServer` and tell
it where your source files are, and it'll run tests and reload code via
an http server. You can also run a single test independent from the rest
of the file.

See [the example app's implementation](https://github.com/Cue/hegemon-example/blob/master/src/test/java/com/cueup/hegemon/example/ExampleJsTestServer.java) for more.

Add with maven:

```xml
<dependencies>
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-testserver</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```


### hegemon-annotations

When any of your Java project can be called easily through JavaScript,
sometimes JS is the only caller and your IDE misidentifies dead code.

The `@ReferencedByJavascript` annotation is a simple way to advertise
that the code is called somewhere. Long term it might be nice to build
tools around this.

Conceivably there will be other annotations in the future, but for now
the `hegemon-annotations` project is separated to simplify project
dependencies.


### hegemon-guice

The `hegemon-guice` project is for pre-annotated classes useful to
Google Guice users. For now, it's just `InjectableScriptCache` - a
singleton that recieves it's `LoadPath` via injection.


Add it to your project with maven:

```xml
<dependencies>
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-guice</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
```
