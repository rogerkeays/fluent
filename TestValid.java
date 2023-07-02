
import java.util.Arrays;
import static java.util.Collections.sort;

// reminder: use `java -ea or -enableassertions` to execute
public class TestValid {
    public static void main(String [] args) {

        // native types
        //assert 0.inc().equals(inc(0));  // can't parse this: ambiguous
        assert (0).inc() == inc(0);
        assert 0f.incf() == incf(0f);
        assert true.invert() == invert(true);
        
        // boxed types
        assert Integer.valueOf(0).incBoxed().equals(incBoxed(Integer.valueOf(0)));
        assert Float.valueOf(0).incfBoxed().equals(incfBoxed(Float.valueOf(0)));
        assert Boolean.TRUE.invertBoxed().equals(invertBoxed(Boolean.TRUE));

        // string literals
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

    public static int inc(int i) { return i + 1; }
    public static float incf(float f) { return f + 1.0f; }
    public static boolean invert(boolean b) { return !b; }
    public static Integer incBoxed(Integer i) { return i + 1; }
    public static Float incfBoxed(Float f) { return f + 1.0f; }
    public static Boolean invertBoxed(Boolean b) { return !b; }
    public static String duplicate(String x) { return duplicate(x, false); }
    public static String duplicate(String x, boolean spaces) {
        return spaces ? (x + " " + x) : (x + x);
    }
}

