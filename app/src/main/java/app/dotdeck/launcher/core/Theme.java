package app.dotdeck.launcher.core;

/** Colours and shape language of one look. Pure data; views read it, nothing mutates it. */
public final class Theme {
    public final String id;
    public final String name;
    /** True when the system bars need dark icons (light backgrounds). */
    public final boolean lightBars;
    public final int bg;
    public final int panel;
    public final int ink;
    public final int dim;
    public final int line;
    public final int accent;
    public final int onAccent;
    /** Multiplier on every corner radius; 0 is hard-edged. */
    public final float radius;
    /** Panels get a hairline border instead of relying on a fill. */
    public final boolean outlined;
    /** Clock and swatch dots are squares, like a segment display. */
    public final boolean squareDots;
    /** Headlines use the monospace face. */
    public final boolean monoHeadline;

    private Theme(Builder b) {
        id = b.id; name = b.name; lightBars = b.lightBars;
        bg = b.bg; panel = b.panel; ink = b.ink; dim = b.dim; line = b.line;
        accent = b.accent; onAccent = b.onAccent;
        radius = b.radius; outlined = b.outlined; squareDots = b.squareDots; monoHeadline = b.monoHeadline;
    }

    public static Builder builder(String id, String name) { return new Builder(id, name); }

    public static final class Builder {
        private final String id;
        private final String name;
        private boolean lightBars;
        private int bg, panel, ink, dim, line, accent, onAccent;
        private float radius = 1f;
        private boolean outlined, squareDots, monoHeadline;

        private Builder(String id, String name) { this.id = id; this.name = name; }

        public Builder palette(int bg, int panel, int ink, int dim, int line) {
            this.bg = bg; this.panel = panel; this.ink = ink; this.dim = dim; this.line = line;
            return this;
        }

        public Builder accent(int accent, int onAccent) { this.accent = accent; this.onAccent = onAccent; return this; }
        public Builder lightBars() { lightBars = true; return this; }
        public Builder radius(float scale) { radius = scale; return this; }
        public Builder outlined() { outlined = true; return this; }
        public Builder squareDots() { squareDots = true; return this; }
        public Builder monoHeadline() { monoHeadline = true; return this; }
        public Theme build() { return new Theme(this); }
    }
}
