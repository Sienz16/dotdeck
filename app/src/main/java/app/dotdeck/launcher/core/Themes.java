package app.dotdeck.launcher.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The built-in looks. Original palettes in a monochrome, dot-matrix spirit; not copied from any vendor.
 * Every colour pair is checked for legibility in the core tests.
 */
public final class Themes {
    private static final int RED = 0xFFD71921;

    public static final Theme DARK = Theme.builder("dark", "DARK")
            .palette(0xFF111110, 0xFF1C1C1A, 0xFFF0EEE6, 0xFFA3A39A, 0xFF343430)
            .accent(RED, 0xFFFFFFFF).build();

    public static final Theme LIGHT = Theme.builder("light", "LIGHT")
            .palette(0xFFF1F0EA, 0xFFE5E4DD, 0xFF171715, 0xFF55554F, 0xFFD0CFC7)
            .accent(RED, 0xFFFFFFFF).lightBars().build();

    /** True black with hairline tiles: easy on OLED panels at night. */
    public static final Theme VOID = Theme.builder("void", "VOID")
            .palette(0xFF000000, 0xFF000000, 0xFFFFFFFF, 0xFFA0A0A0, 0xFF3A3A3A)
            .accent(RED, 0xFFFFFFFF).radius(.4f).outlined().build();

    /** Paper white with hairline tiles. */
    public static final Theme MILK = Theme.builder("milk", "MILK")
            .palette(0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF595959, 0xFFCFCFCF)
            .accent(RED, 0xFFFFFFFF).lightBars().radius(.4f).outlined().build();

    /** Red field, white ink. */
    public static final Theme SIGNAL = Theme.builder("signal", "SIGNAL")
            .palette(0xFFA80E17, 0xFF820B12, 0xFFFFFFFF, 0xFFFFD6D8, 0xFF700910)
            .accent(0xFFFFFFFF, 0xFFA80E17).build();

    /** Green phosphor terminal: square dots, hard edges, monospace headlines. */
    public static final Theme TERMINAL = Theme.builder("terminal", "TERMINAL")
            .palette(0xFF050A05, 0xFF0B140B, 0xFF5CFF7A, 0xFF3FA356, 0xFF1A2E1A)
            .accent(0xFF5CFF7A, 0xFF050A05).radius(0f).outlined().squareDots().monoHeadline().build();

    /** Amber segment display, like an old dash cluster. */
    public static final Theme AMBER = Theme.builder("amber", "AMBER")
            .palette(0xFF0D0800, 0xFF1A1100, 0xFFFFB000, 0xFFC08400, 0xFF3A2600)
            .accent(0xFFFFB000, 0xFF0D0800).radius(.5f).squareDots().monoHeadline().build();

    /** Engineering drawing: navy, pale lines, cyan highlight. */
    public static final Theme BLUEPRINT = Theme.builder("blueprint", "BLUEPRINT")
            .palette(0xFF0B1F3A, 0xFF12335E, 0xFFE8F1FF, 0xFF8FB4E8, 0xFF2A5080)
            .accent(0xFF4DA3FF, 0xFF04101F).radius(.5f).outlined().monoHeadline().build();

    /** Warm grey with an orange accent. */
    public static final Theme EMBER = Theme.builder("ember", "EMBER")
            .palette(0xFFE9E9E6, 0xFFD9D9D5, 0xFF111111, 0xFF55554F, 0xFFC2C2BC)
            .accent(0xFFD94400, 0xFFFFFFFF).lightBars().radius(.7f).build();

    private static final List<Theme> ALL = Collections.unmodifiableList(
            Arrays.asList(DARK, LIGHT, VOID, MILK, SIGNAL, TERMINAL, AMBER, BLUEPRINT, EMBER));

    private Themes() { }

    public static List<Theme> all() { return ALL; }

    /** Unknown or missing ids fall back to DARK. */
    public static Theme byId(String id) {
        for (Theme theme : ALL) { if (theme.id.equals(id)) { return theme; } }
        return DARK;
    }

    /** "03 / VOID": the position in the picker plus the name, shown in the home header. */
    public static String label(Theme theme) {
        return String.format(Locale.ROOT, "%02d / %s", ALL.indexOf(theme) + 1, theme.name);
    }
}
