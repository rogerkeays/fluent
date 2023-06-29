
import static java.lang.System.out;

public class test {
    public static void main(String [] args) {

        // valid usage
        out.println("hello".duplicateEX());
        out.println("hello".duplicateEX(3));

        String hello = "hello";
        out.println(hello.duplicateEX());
        out.println(hello.duplicateEX().duplicateEX());
        out.println(hello.duplicateEX().duplicateEX().duplicateEX());

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

