package app.dotdeck.launcher.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Immutable, sorted snapshot of the launchable apps. A reload swaps in a new catalog. */
public final class AppCatalog {
    public static final AppCatalog EMPTY = new AppCatalog(Collections.<AppEntry>emptyList());

    private final List<AppEntry> apps;
    private final Map<String, AppEntry> byKey = new HashMap<>();

    public AppCatalog(List<AppEntry> apps) {
        this.apps = Collections.unmodifiableList(new ArrayList<>(apps));
        for (AppEntry app : this.apps) { byKey.put(app.key, app); }
    }

    public boolean isEmpty() { return apps.isEmpty(); }

    public List<AppEntry> all() { return apps; }

    /** @return the app, or null for an empty, unknown or uninstalled key */
    public AppEntry find(String key) { return key == null ? null : byKey.get(key); }

    public List<AppEntry> search(String query) { return AppEntry.filter(apps, query); }
}
