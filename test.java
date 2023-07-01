
import static java.lang.System.out;

// reminder: use `java -ea or -enableassertions` for testing
public class test {
    public static void main(String [] args) {
        String hello = "hello";

        // standard java: literals
        assert "hello".toUpperCase().equals("HELLO");
        assert duplicate("hello").equals("hellohello");
        assert duplicate("hello", true).equals("hello hello");
        assert duplicate("hello").toUpperCase().equals("HELLOHELLO");
        assert duplicate("hello".toUpperCase()).equals("HELLOHELLO");
        assert duplicate(duplicate("hello")).equals("hellohellohellohello");

        // standard java: variables
        assert hello.toUpperCase().equals("HELLO");
        assert duplicate(hello).equals("hellohello");
        assert duplicate(hello, true).equals("hello hello");
        assert duplicate(hello).toUpperCase().equals("HELLOHELLO");
        assert duplicate(hello.toUpperCase()).equals("HELLOHELLO");
        assert duplicate(duplicate(hello)).equals("hellohellohellohello");

        // fluent java: literals
        assert "hello".duplicate().equals("hellohello");
        assert "hello".duplicate(true).equals("hello hello");
        assert "hello".duplicate().toUpperCase().equals("HELLOHELLO");
        assert "hello".toUpperCase().duplicate().equals("HELLOHELLO");
        assert "hello".duplicate().duplicate().equals("hellohellohellohello");

        // fluent java: variables
        assert hello.duplicate().equals("hellohello");
        assert hello.duplicate(true).equals("hello hello");
        assert hello.duplicate().toUpperCase().equals("HELLOHELLO");
        assert hello.toUpperCase().duplicate().equals("HELLOHELLO");
        assert hello.duplicate().duplicate().equals("hellohellohellohello");

        // expected compilation errors
        // "hello".blah();
        // blah("hello");
        // hello.duplicate(3);
        // hello.duplicate(true, 4);
    }

    public static String duplicate(String x) { return duplicate(x, false); }
    public static String duplicate(String x, boolean spaces) {
        return spaces ? (x + " " + x) : (x + x);
    }
}

