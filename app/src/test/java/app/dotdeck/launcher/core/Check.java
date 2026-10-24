package app.dotdeck.launcher.core;

/** Tiny assertion helper shared by the core tests. */
final class Check {
    private static int count;

    private Check() { }

    static void equal(Object expected, Object actual, String message) {
        count++;
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    static void isTrue(boolean condition, String message) {
        count++;
        if (!condition) { throw new AssertionError(message); }
    }

    static int count() { return count; }
}
