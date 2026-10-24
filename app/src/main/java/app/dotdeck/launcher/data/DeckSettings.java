package app.dotdeck.launcher.data;

import android.content.SharedPreferences;
import app.dotdeck.launcher.core.LayoutStyle;
import app.dotdeck.launcher.core.Theme;
import app.dotdeck.launcher.core.Themes;

/** Typed access to the user's preferences, so no other class knows a preference key. */
public final class DeckSettings {
    private static final String THEME = "theme";
    private static final String LAYOUT = "layout";
    private static final String MOTION = "motion";
    private static final String LEGACY_LIGHT = "light";
    private static final String CLOCK_24 = "clock24";
    private static final String MONO_ICONS = "mono";

    private final SharedPreferences preferences;

    public DeckSettings(SharedPreferences preferences) { this.preferences = preferences; }

    public Theme theme() {
        String id = preferences.getString(THEME, null);
        if (id == null) { id = preferences.getBoolean(LEGACY_LIGHT, false) ? Themes.LIGHT.id : Themes.DARK.id; }
        return Themes.byId(id);
    }

    public void setTheme(Theme theme) { preferences.edit().putString(THEME, theme.id).apply(); }

    public LayoutStyle layout() { return LayoutStyle.byId(preferences.getString(LAYOUT, null)); }

    public void setLayout(LayoutStyle style) { preferences.edit().putString(LAYOUT, style.id).apply(); }

    public boolean motion() { return preferences.getBoolean(MOTION, true); }

    public void toggleMotion() { preferences.edit().putBoolean(MOTION, !motion()).apply(); }

    public boolean clock24() { return preferences.getBoolean(CLOCK_24, true); }

    public void toggleClock24() { preferences.edit().putBoolean(CLOCK_24, !clock24()).apply(); }

    public boolean monoIcons() { return preferences.getBoolean(MONO_ICONS, true); }

    public void toggleMonoIcons() { preferences.edit().putBoolean(MONO_ICONS, !monoIcons()).apply(); }
}
