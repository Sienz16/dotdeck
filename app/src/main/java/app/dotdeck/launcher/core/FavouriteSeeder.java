package app.dotdeck.launcher.core;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** First-run guess at which installed app belongs on each tile. */
public final class FavouriteSeeder {
    private FavouriteSeeder() { }

    /** Fills empty tiles only, and never puts the same app on two tiles. */
    public static void seed(FavoriteSlots slots, AppCatalog catalog) {
        Set<String> used = new HashSet<>();
        for (int i = 0; i < FavoriteSlots.COUNT; i++) {
            if (!slots.get(i).isEmpty()) { used.add(slots.get(i)); }
        }
        for (int i = 0; i < FavoriteSlots.COUNT; i++) {
            if (!slots.get(i).isEmpty()) { continue; }
            AppEntry match = firstMatch(Slot.at(i), catalog, used);
            if (match != null) { slots.assign(i, match.key); used.add(match.key); }
        }
    }

    private static AppEntry firstMatch(Slot slot, AppCatalog catalog, Set<String> used) {
        for (String term : slot.seedTerms()) {
            for (AppEntry app : catalog.all()) {
                if (!used.contains(app.key)
                        && (app.key + " " + app.label).toLowerCase(Locale.ROOT).contains(term)) {
                    return app;
                }
            }
        }
        return null;
    }
}
