package app.dotdeck.launcher.core;

import java.util.Arrays;
import java.util.List;

final class CatalogTest {
    private CatalogTest() { }

    static void run() {
        List<AppEntry> apps = Arrays.asList(new AppEntry("p/.Maps", "Maps"),
                new AppEntry("m/.Player", "Music"), new AppEntry("s/.Settings", "Settings"));
        Check.equal(1, AppEntry.filter(apps, "  MAP  ").size(), "search ignores case and surrounding spaces");
        Check.equal(3, AppEntry.filter(apps, "").size(), "empty query returns all apps");
        Check.equal(0, AppEntry.filter(apps, "missing").size(), "no matches is empty");
        Check.equal(1, AppEntry.filter(apps, "m/.Player").size(), "component search works");
        Check.equal(3, apps.size(), "filter preserves source list");

        AppCatalog catalog = new AppCatalog(apps);
        Check.equal("Music", catalog.find("m/.Player").label, "find by key");
        Check.isTrue(catalog.find("") == null, "empty key is not an app");
        Check.isTrue(catalog.find(null) == null, "null key is not an app");
        Check.isTrue(catalog.find("gone/.App") == null, "uninstalled key is not an app");
        Check.equal(1, catalog.search("sett").size(), "catalog search delegates to filter");
        Check.isTrue(AppCatalog.EMPTY.isEmpty(), "EMPTY has no apps");
        try { catalog.all().add(new AppEntry("x", "x")); throw new AssertionError("catalog is mutable"); }
        catch (UnsupportedOperationException expected) { Check.isTrue(true, "catalog is read-only"); }
    }
}
