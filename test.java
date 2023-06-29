
import static java.lang.System.out;

public class test {

    public static void main(String [] args) {
        out.println("hello".duplicate());
        out.println("hello".duplicate(3));

        String hello = "hello";
        out.println(hello.duplicate());
        out.println(hello.duplicate(3, 4));
        out.println(hello.duplicate().duplicate());
        out.println(hello.duplicate().duplicate().duplicate());
    }

    public static String duplicate(String x) {
        return x + " " + x;
    }

    public static String duplicate(String x, int times) {
        return (x + " ").repeat(times);
    }
}

