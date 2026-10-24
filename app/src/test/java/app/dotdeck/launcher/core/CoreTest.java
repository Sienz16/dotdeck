package app.dotdeck.launcher.core;

/** Runs every pure-Java check. Needs no Android SDK: see test.sh. */
public final class CoreTest {
    private CoreTest() { }

    public static void main(String[] args) {
        SlotsTest.run();
        CatalogTest.run();
        SeederTest.run();
        NavStateTest.run();
        ThemeTest.run();
        LayoutStyleTest.run();
        System.out.println("PASS: " + Check.count() + " core regression checks");
    }
}
