# Fluent: Static Extension Methods for Java

*Fluent* allows you to call static Java methods as if they were object methods. For example, instead of writing:

    assertContains(getHttpContent(createUrl(website, "styles.css"), 60), "img.jpg");

you would write:

    website.createUrl("styles.css").getHttpContent(60).assertContains("img.jpg");

*Fluent* works by transforming the abstract syntax tree during compilation. If a method can't be resolved using Java's normal rules, *Fluent* will rewrite it as such:

    object.method(params...) -> method(object, params...)

and then give it back to the compiler. Now, the compiler will look for a static method taking the object as it's first parameter. Any static methods that are in scope can be used. i.e, those you've written or imported. If you are importing them from another class, you will need to use `import static` so they can be resolved. No annotations are required, and you can add extension methods to primitive types.

In the above example, the extension method signatures would be:

    public static URL createUrl(Website website, String path) {}
    public static String getHttpContent(URL url, int timeout) {}
    public static void assertContains(String string, String string) {}

Extension methods are useful when you can't (or don't want to) add methods to a class or subclass, or you are working with an interface. Commonly, such methods are called "utility methods", but in most other programming languages, you would just call them "functions".

*Fluent* is invoked as a `javac` compiler plugin and has no runtime dependencies. The resulting class files are identical to code written with regular static method calls.

*Fluent* supports JDK 9 and above.

## Quick Start

Download the jar, place it on your classpath, and run `javac` with `-Xplugin:fluent` and `-J--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED`:

    wget https://github.com/rogerkeays/fluent/raw/main/fluent.jar
    javac -cp fluent.jar -Xplugin:fluent -J--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED Test.java

Run your code like you always have:

    java Test

## Install Using Maven

*Fluent* is not yet available on Maven Central, however you can install it locally like this:

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
          <version>3.11.0</version>
          <configuration>
            <compilerArgs>
              <arg>-Xplugin:fluent</arg>
              <arg>-J--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED</arg>
            </compilerArgs>
            <fork>true</fork>
            ...
          </configuration>
        </plugin>

Note, older versions of the compiler plugin use a different syntax. Refer to the [Maven Compiler Plugin docs](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html) for more details. Make sure you add the `<fork>true</fork>` option too.

## Build It Yourself

*Fluent* is built using a POSIX shell script:

    git clone https://github.com/rogerkeays/fluent.git
    cd fluent
    ./build.sh

## JDK Support

*Fluent* is tested with the following JDKs:

  * jdk-09.0.4
  * jdk-10.0.2
  * jdk-11.0.8
  * jdk-12.0.2
  * jdk-13.0.2
  * jdk-14.0.2
  * jdk-15.0.2
  * jdk-16.0.2
  * jdk-17.0.2
  * jdk-18.0.2.1
  * jdk-19.0.2
  * jdk-20.0.1
  * jdk-21 (early access)
  * jdk-22 (early access)

To ensure backwards compatibility with existing code, *Fluent* has been used to compile and test the following open source projects:

  * [Apache Commons Lang](https://github.com/apache/commons-lang)
  * [iText](https://github.com/itext/itext7)
  * [Tomcat](https://github.com/apache/tomcat)

## IDE Support

There is currently no IDE support for *Fluent*. Contributions are welcome. It may be possible to get your IDE to load the *Fluent* plugin into it's compiler. If you get it working, please [comment on issue #4](https://github.com/rogerkeays/fluent/issues/4) so we can all benefit.

## Known Issues

  * you must use parentheses around numeric primitives when calling an extension method: e.g. `(0).inc()` 
  * *Fluent* may not be compatible with other `javac` plugins, although it works with Lombok and [Unchecked](https://github.com/rogerkeays/unchecked), at least.
  * If you are using *Fluent* with [Unchecked](https://github.com/rogerkeays/unchecked), we recommend you specify the `-Xplugin:unchecked` option first, as this is how it is tested.

Please submit issues to the [github issue tracker](https://github.com/rogerkeays/fluent/issues). Be sure to include the JDK version and build tools you are using. A snippet of the code causing the problem will help to reproduce the bug. Before submitting, please try a clean build of your project.

## Discussions

Language design can be a divisive topic. Some interesting threads around extension methods can be found here:

  * [Why doesn't Java support extension methods?](https://stackoverflow.com/questions/29466427/what-was-the-design-consideration-of-not-allowing-use-site-injection-of-extensio/29494337#29494337)
  * [Extension Methods on Wikipedia](https://en.wikipedia.org/wiki/Extension_method)
  * [Uniform Function Call Syntax in D](https://tour.dlang.org/tour/en/gems/uniform-function-call-syntax-ufcs)
  * [Fluent post on hackernews](https://news.ycombinator.com/item?id=36569411)
  * [Fluent post on reddit](https://www.reddit.com/r/java/comments/14ole9l/fluent_static_extension_methods_for_java/)

## Related Resources

  * [Kotlin](https://kotlinlang.org): a JVM language which supports extension methods out of the box.
  * [Lombok](https://github.com/projectlombok/lombok): the grand-daddy of `javac` hacks, with experimental support for extension methods.
  * [Manifold](https://manifold.systems): a `javac` plugin with many features, including extension methods.
  * [racket-fluent](https://github.com/rogerkeays/racket-fluent): fluent syntax for Racket.
  * [Unchecked](https://github.com/rogerkeays/unchecked): a similar compiler plugin to disable checked exceptions.
  * [More stuff you never knew you wanted](https://rogerkeays.com).

## Disclaimer

  * *Fluent* is not supported or endorsed by the OpenJDK team.
  * The reasonable man adapts himself to the world. The unreasonable one persists in trying to adapt the world to himself. Therefore all progress depends on the unreasonable man. --George Bernard Shaw

