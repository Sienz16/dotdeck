package app.dotdeck.launcher.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AppEntry {
    public final String key;
    public final String label;

    public AppEntry(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public static List<AppEntry> filter(List<AppEntry> source, String query) {
        String term = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<AppEntry> result = new ArrayList<>();
        for (AppEntry app : source) {
            if (term.isEmpty() || app.label.toLowerCase(Locale.ROOT).contains(term)
                    || app.key.toLowerCase(Locale.ROOT).contains(term)) {
                result.add(app);
            }
        }
        return result;
    }
}
