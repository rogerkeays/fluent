
import static java.lang.System.out;

public class test {
    public static void main(String [] args) {
        String hello = "hello";

        // standard java
        duplicateEX("hello");
        duplicateEX(hello);
        out.println(duplicateEX("hello"));
        out.println(duplicateEX(hello));
        System.out.println(duplicateEX("hello"));
        System.out.println(duplicateEX(hello));

        // standard java with params
        duplicateEX("hello", 3);
        duplicateEX(hello, 3);
        out.println(duplicateEX("hello", 3));
        out.println(duplicateEX(hello, 3));
        System.out.println(duplicateEX("hello", 3));
        System.out.println(duplicateEX(hello, 3));

        // fluent java
        "hello".duplicateEX();
        hello.duplicateEX();
        out.println("hello".duplicateEX());
        out.println(hello.duplicateEX());
        System.out.println("hello".duplicateEX());
        System.out.println(hello.duplicateEX());

        // fluent java with params
        "hello".duplicateEX(3);
        hello.duplicateEX(3);
        out.println("hello".duplicateEX(3));
        out.println(hello.duplicateEX(3));
        System.out.println("hello".duplicateEX(3));
        System.out.println(hello.duplicateEX(3));

        // standard java with chaining
        // fluent java with chaining
        //out.println(hello.duplicateEX().duplicateEX());
        //out.println(hello.duplicateEX ().duplicateEX(). duplicateEX();

        // invalid usages (should not compile)
        //out.println(hello.duplicateEX(3, 4));
    }

    public static String duplicateEX(String x) {
        return x + " " + x;
    }

    public static String duplicateEX(String x, int times) {
        return (x + " ").repeat(times);
    }
}

