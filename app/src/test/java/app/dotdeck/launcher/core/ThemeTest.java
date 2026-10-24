package app.dotdeck.launcher.core;

import java.util.HashSet;
import java.util.Set;

final class ThemeTest {
    private ThemeTest() { }

    static void run() {
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (Theme theme : Themes.all()) {
            Check.isTrue(ids.add(theme.id), "unique id " + theme.id);
            Check.isTrue(names.add(theme.name), "unique name " + theme.name);
            Check.equal(theme, Themes.byId(theme.id), "lookup by id " + theme.id);
            legible(theme);
        }
        Check.equal(Themes.DARK, Themes.byId("nonsense"), "unknown id falls back to dark");
        Check.equal(Themes.DARK, Themes.byId(null), "null id falls back to dark");
        Check.equal("03 / VOID", Themes.label(Themes.VOID), "header label");
    }

    /** WCAG contrast: body text 4.5, primary text 7, accent graphics 3. */
    private static void legible(Theme t) {
        atLeast(t, "ink on bg", t.ink, t.bg, 7);
        atLeast(t, "ink on panel", t.ink, t.panel, 7);
        atLeast(t, "dim on bg", t.dim, t.bg, 4.5);
        atLeast(t, "dim on panel", t.dim, t.panel, 4.5);
        atLeast(t, "label on accent", t.onAccent, t.accent, 3.9);
        atLeast(t, "accent on panel", t.accent, t.panel, 3);
        atLeast(t, "accent on bg", t.accent, t.bg, 3);
        atLeast(t, "lit clock dot vs unlit dot", t.ink, t.line, 1.5);
    }

    private static void atLeast(Theme theme, String what, int fore, int back, double minimum) {
        double ratio = ratio(fore, back);
        Check.isTrue(ratio >= minimum, theme.id + " " + what + " contrast " + ratio + " < " + minimum);
    }

    private static double ratio(int a, int b) {
        double la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + .05) / (Math.min(la, lb) + .05);
    }

    private static double luminance(int argb) {
        return .2126 * channel(argb >> 16) + .7152 * channel(argb >> 8) + .0722 * channel(argb);
    }

    private static double channel(int value) {
        double c = (value & 0xFF) / 255d;
        return c <= .03928 ? c / 12.92 : Math.pow((c + .055) / 1.055, 2.4);
    }
}
