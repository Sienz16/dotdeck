package app.dotdeck.launcher.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One favourite tile: its look, its tagline and the search terms used to pre-fill it on first run. */
public enum Slot {
    NAVIGATION("NAVIGATION", Glyph.NAV, "Find your way", "waze", "maps", "navigation"),
    AUDIO("AUDIO", Glyph.MUSIC, "Set the soundtrack", "spotify", "music", "radio"),
    PHONE("PHONE", Glyph.PHONE, "Stay connected", "phone", "bluetooth", "dialer"),
    PROJECTION("PROJECTION", Glyph.PROJECTION, "Connect your phone", "zlink", "tlink", "carplay", "autokit"),
    CAMERA("CAMERA", Glyph.CAMERA, "A different view", "camera", "360"),
    YOUR_PICK("YOUR PICK", Glyph.PLUS, "Make it yours", "settings");

    public final String title;
    public final Glyph glyph;
    public final String hint;
    private final List<String> seedTerms;

    Slot(String title, Glyph glyph, String hint, String... seedTerms) {
        this.title = title;
        this.glyph = glyph;
        this.hint = hint;
        this.seedTerms = Collections.unmodifiableList(Arrays.asList(seedTerms));
    }

    public List<String> seedTerms() { return seedTerms; }

    /** The first tile carries the accent colour. */
    public boolean accented() { return this == NAVIGATION; }

    public static Slot at(int index) { return values()[index]; }
}
