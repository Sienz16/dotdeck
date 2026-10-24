package app.dotdeck.launcher.core;

final class NavStateTest {
    private NavStateTest() { }

    static void run() {
        NavState nav = new NavState();
        Check.equal(Page.HOME, nav.page(), "starts on home");
        nav.showDrawer(2);
        nav.setQuery("map");
        Check.equal(2, nav.picking(), "drawer remembers the tile being filled");
        Check.isTrue(nav.isPicking(), "picking flag follows the slot");
        nav.showSettings();
        Check.equal("", nav.query(), "leaving the drawer clears the search");
        Check.isTrue(!nav.isPicking(), "settings is never picking");
        nav.showDrawer(99);
        Check.isTrue(!nav.isPicking(), "out-of-range slot becomes browsing");
        nav.showHome();
        Check.equal(Page.HOME, nav.page(), "home");

        NavState restored = NavState.restore(Page.DRAWER.ordinal(), 3, "cam");
        Check.equal(3, restored.picking(), "restored picking slot");
        Check.equal("cam", restored.query(), "restored query");
        NavState corrupt = NavState.restore(42, 7, null);
        Check.equal(Page.HOME, corrupt.page(), "unknown page falls back to home");
        Check.equal("", corrupt.query(), "null query becomes empty");
        NavState stray = NavState.restore(Page.SETTINGS.ordinal(), 2, "x");
        Check.isTrue(!stray.isPicking() && stray.query().isEmpty(), "stale drawer state dropped outside the drawer");
    }
}
