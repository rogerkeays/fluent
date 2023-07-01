
public class testErrors {
    public static void main(String [] args) {
        blah("hello"); // no such method
        "hello".blah(); // no such method
        duplicate("hello", 3); // incorrect parameter type
        "hello".duplicate(3); // incorrect parameter type
        duplicate("hello", true, 4); // incorrect parameter count
        "hello".duplicate(true, 4); // incorrect parameter count
    }

    public static String duplicate(String x) { return duplicate(x, false); }
    public static String duplicate(String x, boolean spaces) {
        return spaces ? (x + " " + x) : (x + x);
    }
}

