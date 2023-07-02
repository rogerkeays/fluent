# fluent: Static Extension Methods for Java

*fluent* allows you to call static Java methods as if they were object methods. For example, instead of writing:

    assertNotEmpty(getHttpContent(createSharedUrl(website, "styles.css")));

you would write:

    website.createSharedUrl("styles.css").getHttpContent().assertNotEmpty();

where the method signatures are:

    public static URL createSharedUrl(Website website, String path) {}
    public static String getHttpContent(URL url) {}
    public static void assertNotEmpty(String string) {}

*fluent* works by passing the object instance as the first parameter to the static extension method. **This only occurs if normal method resolution fails**. No annotations are required. You can use any static method as an extension, although if you are importing them from another class, you will need to use `import static` so they can be resolved.

Extension methods are useful for the case where you can't (or don't want to) add methods to a class or subclass. Commonly, such methods are called "utility methods", but they can be better thought of as "functions".

*fluent* is implemented as a `javac` compiler plugin and has no runtime dependencies. It works by transforming the abstract syntax tree during compilation, so the resulting class file is identical to writing native static method calls.

*fluent* requires jdk 9 or above.

## Quick Start

Download the jar, place it on your classpath, and run `javac` using `-Xplugin:fluent`:

    wget https://github.com/rogerkeays/fluent/raw/main/fluent.jar
    javac -cp fluent.jar -Xplugin:fluent File.java

## Install Using Maven

*fluent* is not yet available on Maven Central, however you can install it locally like this:

    wget https://github.com/rogerkeays/fluent/raw/main/fluent.jar
    mvn install:install-file -DgroupId=jamaica -DartifactId=fluent -Dversion=0.1.0 -Dpackaging=jar -Dfile=fluent.jar
    
Next, add the dependency to your `pom.xml`:

    <dependency>
      <groupId>jamaica</groupId>
      <artifactId>fluent</artifactId>
      <version>0.1.0</version>
      <scope>compile</scope>
    </dependency>

And configure the compiler plugin:

    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>2.3.2</version>
          <configuration>
            <compilerArguments>
              <Xplugin:fluent/>
            </compilerArguments>
            ...
          </configuration>
        </plugin>

Note, newer versions of the compiler plugin use a different syntax:

     <compilerArgs>
       <arg>-Xplugin:fluent</arg>
     </compilerArgs>

You may also have to experiment with `<fork/>` and `<forceJavacCompilerUse/>` to get Maven to co-operate. Refer to the [Maven Compiler Plugin docs](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html) for more details.

## Build It Yourself

*fluent* is built using a `bash` script:

    git clone https://github.com/rogerkeays/fluent.git
    cd fluent
    ./build.sh

If your operating system doesn't include `bash` it shouldn't be too hard to convert to whatever shell you are using. I mean, we're talking about one java file and a text file here.

## IDE Support

There is currently no IDE support for *fluent*. Part of the purpose of this plugin is to make Java easier, so you don't need an IDE. However, contributions are welcome. It may be possible to get your IDE to load the *fluent* plugin into it's compiler. If you get it working, please [post something to github](https://github.com/rogerkeays/fluent/issues) so we can all benefit.

## Known Issues

   * *fluent* may not be compatible with other `javac` plugins, though so far it seems to play nice with Lombok, at least.
   * *fluent* will make you a more productive programmer, which may go against corporate policy.

## Related Resources

   * [kotlin](https://kotlinlang.org): a jvm language which supports extension methods out of the box.
   * [Project Lombok](https://github.com/projectlombok/lombok): the grand-daddy of `javac` hacks.
   * [unchecked](https://github.com/rogerkeays/unchecked): evade the checked exceptions mafia in Java.
   * [Java Operator Overloading](https://github.com/amelentev/java-oo): a `javac` plugin using similar ideas.
   * [racket-fluent](https://github.com/rogerkeays/racket-fluent): fluent syntax for Racket.
   * [more stuff you never knew you wanted](https://rogerkeays.com)

