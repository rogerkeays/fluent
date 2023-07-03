# Fluent: Static Extension Methods for Java

*Fluent* allows you to call static Java methods as if they were object methods. For example, instead of writing:

    assertNotEmpty(getHttpContent(createUrl(website, "styles.css")));

you would write:

    website.createUrl("styles.css").getHttpContent().assertNotEmpty();

*Fluent* works by transforming the abstract syntax tree during compilation. If a method can't be resolved using Java's normal rules, *Fluent* will rewrite it as such:

    object.method(params...) -> method(object, params...)

and then give it back to the compiler. Now, the compiler will look for a static method taking the object as it's first parameter. Any static methods that are in scope can be used. i.e, those you've written or imported. If you are importing them from another class, you will need to use `import static` so they can be resolved. No annotations are required, and you can add extension methods to primitive types.

In the above example, the extension method signatures would be:

    public static URL createUrl(Website website, String path) {}
    public static String getHttpContent(URL url) {}
    public static void assertNotEmpty(String string) {}

Extension methods are useful when you can't (or don't want to) add methods to a class or subclass, or you are working with an interface. Commonly, such methods are called "utility methods", but in most other programming languages, you would just call them "functions".

*Fluent* is implemented as a `javac` compiler plugin and has no runtime dependencies. The resulting class files are identical to code written with regular static method calls.

*Fluent* supports JDK 9 and above.

## Quick Start

Download the jar, place it on your classpath, and run `javac` using `-Xplugin:fluent`:

    wget https://github.com/rogerkeays/fluent/raw/main/fluent.jar
    javac -cp fluent.jar -Xplugin:fluent File.java

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
            </compilerArg>
            ...
          </configuration>
        </plugin>

Note, older versions of the compiler plugin use a different syntax. Refer to the [Maven Compiler Plugin docs](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html) for more details.

## Build It Yourself

*Fluent* is built using a POSIX shell script:

    git clone https://github.com/rogerkeays/fluent.git
    cd fluent
    ./build.sh

If your operating system doesn't include `sh` it shouldn't be too hard to convert to whatever shell you are using. I mean, we're talking about one java file and a text file here.

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

## IDE Support

There is currently no IDE support for *Fluent*. Contributions are welcome. It may be possible to get your IDE to load the *Fluent* plugin into it's compiler. If you get it working, please [post something to github](https://github.com/rogerkeays/fluent/issues) so we can all benefit.

## Known Issues

  * you must use parentheses around numeric primitives when calling an extension method: e.g. `(0).inc()` 
  * *Fluent* may not be compatible with other `javac` plugins, though so far it seems to play nice with Lombok, at least.
  * *Fluent* will make you a more productive programmer, which may go against corporate policy.

## Related Resources

  * [kotlin](https://kotlinlang.org): a JVM language which supports extension methods out of the box.
  * [Project Lombok](https://github.com/projectlombok/lombok): the grand-daddy of `javac` hacks.
  * [unchecked](https://github.com/rogerkeays/unchecked): evade the checked exceptions mafia in Java.
  * [Java Operator Overloading](https://github.com/amelentev/java-oo): a `javac` plugin using similar ideas.
  * [racket-fluent](https://github.com/rogerkeays/racket-fluent): fluent syntax for Racket.
  * [more stuff you never knew you wanted](https://rogerkeays.com)

