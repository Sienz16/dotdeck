package app.dotdeck.launcher.data;

import android.content.SharedPreferences;
import app.dotdeck.launcher.core.AppCatalog;
import app.dotdeck.launcher.core.FavoriteSlots;
import app.dotdeck.launcher.core.FavouriteSeeder;

/** The six favourite tiles, persisted on every change. */
public final class FavouriteStore {
    private static final String SLOT_PREFIX = "slot_";
    private static final String SEEDED = "seeded";

    private final SharedPreferences preferences;
    private final FavoriteSlots slots;

    public FavouriteStore(SharedPreferences preferences) {
        this.preferences = preferences;
        String[] saved = new String[FavoriteSlots.COUNT];
        for (int i = 0; i < saved.length; i++) { saved[i] = preferences.getString(SLOT_PREFIX + i, ""); }
        slots = new FavoriteSlots(saved);
    }

    public String get(int slot) { return slots.get(slot); }

    public void assign(int slot, String key) {
        slots.assign(slot, key);
        save();
    }

    public void clear(int slot) { assign(slot, ""); }

    /** Pre-fills the tiles once, from a catalog that is known to be complete. */
    public void seedIfNeeded(AppCatalog catalog) {
        if (preferences.getBoolean(SEEDED, false) || catalog.isEmpty()) { return; }
        FavouriteSeeder.seed(slots, catalog);
        save();
        preferences.edit().putBoolean(SEEDED, true).apply();
    }

    private void save() {
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < FavoriteSlots.COUNT; i++) { editor.putString(SLOT_PREFIX + i, slots.get(i)); }
        editor.apply();
    }
}
