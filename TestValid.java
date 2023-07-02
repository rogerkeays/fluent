
import java.util.Arrays;
import static java.util.Collections.sort;

// reminder: use `java -ea or -enableassertions` to execute
public class TestValid {
    public static void main(String [] args) {
        
        // literals
        assert "hello".duplicate().equals(duplicate("hello"));
        assert "hello".duplicate(true).equals(duplicate("hello", true));
        assert "hello".duplicate().toUpperCase().equals(duplicate("hello").toUpperCase());
        assert "hello".toUpperCase().duplicate().equals(duplicate("hello".toUpperCase()));
        assert "hello".duplicate().duplicate().equals(duplicate(duplicate("hello")));

        // variables
        String hello = "hello";
        assert hello.duplicate().equals(duplicate(hello));
        assert hello.duplicate(true).equals(duplicate(hello, true));
        assert hello.duplicate().toUpperCase().equals(duplicate(hello).toUpperCase());
        assert hello.toUpperCase().duplicate().equals(duplicate(hello.toUpperCase()));
        assert hello.duplicate().duplicate().equals(duplicate(duplicate(hello)));

        // static imports
        sort(Arrays.asList(3, 2, 1));
        Arrays.asList(3, 2, 1).sort();
    }

    public static String duplicate(String x) { return duplicate(x, false); }
    public static String duplicate(String x, boolean spaces) {
        return spaces ? (x + " " + x) : (x + x);
    }
}

