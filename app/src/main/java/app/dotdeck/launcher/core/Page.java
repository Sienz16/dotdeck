package app.dotdeck.launcher.core;

public enum Page {
    HOME, DRAWER, SETTINGS;

    /** Unknown ordinals (corrupt saved state) fall back to HOME. */
    public static Page fromOrdinal(int ordinal) {
        Page[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : HOME;
    }
}
