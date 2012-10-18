# Hegemon

hegemon is a set of tools and libraries for running the Rhino javascript implementation on the JVM.

For examples of what it can do and how to use it, see the [example application][https://github.com/Cue/hegemon-example].

Add it to your project with maven:

```xml
<dependencies>
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-core</artifactId>
    <version>0.0.1</version>
  </dependency>
  <!-- for JUnit integration -->
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-testing</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
  </dependency>
  <!-- for the HTTP test server -->
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-testserver</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
  </dependency>
  <!-- for the ReferencedByJavascript annotation -->
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-annotations</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
  </dependency>
  <!-- for a pre @Inject'd @Singleton'd ScriptCache -->
  <dependency>
    <groupId>com.cueup.hegemon</groupId>
    <artifactId>hegemon-guice</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```
