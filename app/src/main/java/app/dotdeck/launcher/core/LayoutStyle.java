package app.dotdeck.launcher.core;

/** How the home screen is arranged. Independent of the colour theme. */
public enum LayoutStyle {
    DECK("deck", "DECK", "Clock left, six tiles right"),
    DOCK("dock", "DOCK", "Big clock, round dock below"),
    LIST("list", "LIST", "Clock and a tidy list"),
    BENTO("bento", "BENTO", "Clock as a tile in a box grid"),
    STRIP("strip", "STRIP", "Clock above a scrolling strip");

    public final String id;
    public final String label;
    public final String blurb;

    LayoutStyle(String id, String label, String blurb) {
        this.id = id;
        this.label = label;
        this.blurb = blurb;
    }

    /** Unknown or missing ids fall back to DECK. */
    public static LayoutStyle byId(String id) {
        for (LayoutStyle style : values()) { if (style.id.equals(id)) { return style; } }
        return DECK;
    }
}
