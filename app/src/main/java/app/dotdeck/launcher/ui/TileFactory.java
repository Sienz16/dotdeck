package app.dotdeck.launcher.ui;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.dotdeck.launcher.core.AppEntry;
import app.dotdeck.launcher.core.FavoriteSlots;
import app.dotdeck.launcher.core.Glyph;
import app.dotdeck.launcher.core.Slot;
import app.dotdeck.launcher.core.Theme;
import java.util.ArrayList;
import java.util.List;

import static app.dotdeck.launcher.ui.Ui.MATCH;
import static app.dotdeck.launcher.ui.Ui.WRAP;

/** Builds the six favourite tiles in one of three shapes. Remembers them for the entrance animation. */
final class TileFactory {
    enum Style {
        /** Number, glyph and two lines of text on a panel. */
        CARD,
        /** A round (or square, on hard-edged themes) glyph button with a caption underneath. */
        CHIP,
        /** One wide row: number, glyph, text, arrow. */
        ROW
    }

    private final ScreenContext ctx;
    private final Ui ui;
    private final List<View> created = new ArrayList<>();

    TileFactory(ScreenContext ctx) {
        this.ctx = ctx;
        this.ui = ctx.ui;
    }

    /** Every tile built so far, in order. */
    List<View> created() { return created; }

    List<View> all(Style style) {
        List<View> tiles = new ArrayList<>();
        for (int i = 0; i < FavoriteSlots.COUNT; i++) { tiles.add(tile(i, style)); }
        return tiles;
    }

    View tile(int index, Style style) {
        View tile;
        switch (style) {
            case CHIP: tile = chip(index); break;
            case ROW: tile = row(index); break;
            default: tile = card(index); break;
        }
        bind(tile, index);
        created.add(tile);
        return tile;
    }

    private View card(int index) {
        Theme theme = ui.theme;
        Slot slot = Slot.at(index);
        AppEntry app = ctx.favouriteApp(index);
        int pad = ui.dp(ui.compact ? 10 : 16);
        LinearLayout tile = ui.vertical();
        tile.setPadding(ui.dp(14), pad, ui.dp(14), pad);
        tile.setBackground(ui.panel(20));

        LinearLayout top = ui.horizontal();
        top.addView(ui.mono("0" + (index + 1), 9, theme.dim), new LinearLayout.LayoutParams(0, WRAP, 1));
        top.addView(new GlyphView(ui.context, Glyph.ARROW, theme.dim), new LinearLayout.LayoutParams(ui.dp(14), ui.dp(14)));
        tile.addView(top, new LinearLayout.LayoutParams(MATCH, WRAP));

        FrameLayout symbol = new FrameLayout(ui.context);
        int glyphSize = ui.dp(ui.compact ? 26 : 34);
        symbol.addView(new GlyphView(ui.context, slot.glyph, glyphColor(slot)),
                new FrameLayout.LayoutParams(glyphSize, glyphSize, Gravity.CENTER_VERTICAL));
        tile.addView(symbol, new LinearLayout.LayoutParams(MATCH, 0, 1));

        TextView label = ui.text(title(index, app), ui.compact ? 12 : 15, theme.ink);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        oneLine(label);
        tile.addView(label);
        if (!ui.compact) { tile.addView(subtitle(index, app), topMargin(4)); }
        return tile;
    }

    private View chip(int index) {
        Theme theme = ui.theme;
        Slot slot = Slot.at(index);
        AppEntry app = ctx.favouriteApp(index);
        int size = ui.dp(ui.compact ? 52 : 68);
        LinearLayout chip = ui.vertical();
        chip.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout button = new FrameLayout(ui.context);
        button.setBackground(ui.chip(size));
        int glyphSize = Math.round(size * .45f);
        button.addView(new GlyphView(ui.context, slot.glyph, glyphColor(slot)),
                new FrameLayout.LayoutParams(glyphSize, glyphSize, Gravity.CENTER));
        chip.addView(button, new LinearLayout.LayoutParams(size, size));
        TextView label = ui.text(title(index, app), ui.compact ? 10 : 12, theme.ink);
        label.setGravity(Gravity.CENTER);
        oneLine(label);
        chip.addView(label, topMargin(8));
        return chip;
    }

    private View row(int index) {
        Theme theme = ui.theme;
        Slot slot = Slot.at(index);
        AppEntry app = ctx.favouriteApp(index);
        LinearLayout row = ui.horizontal();
        row.setPadding(ui.dp(16), ui.dp(ui.compact ? 4 : 10), ui.dp(16), ui.dp(ui.compact ? 4 : 10));
        row.setBackground(ui.panel(16));
        row.addView(ui.mono("0" + (index + 1), 10, theme.dim), new LinearLayout.LayoutParams(ui.dp(30), WRAP));
        int glyphSize = ui.dp(ui.compact ? 22 : 28);
        LinearLayout.LayoutParams glyphParams = new LinearLayout.LayoutParams(glyphSize, glyphSize);
        glyphParams.rightMargin = ui.dp(16);
        row.addView(new GlyphView(ui.context, slot.glyph, glyphColor(slot)), glyphParams);
        LinearLayout copy = ui.vertical();
        TextView label = ui.text(title(index, app), ui.compact ? 14 : 17, theme.ink);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        oneLine(label);
        copy.addView(label);
        if (!ui.compact) { copy.addView(subtitle(index, app), topMargin(3)); }
        row.addView(copy, new LinearLayout.LayoutParams(0, WRAP, 1));
        row.addView(new GlyphView(ui.context, Glyph.ARROW, theme.dim), new LinearLayout.LayoutParams(ui.dp(16), ui.dp(16)));
        return row;
    }

    private void bind(View tile, int index) {
        AppEntry app = ctx.favouriteApp(index);
        tile.setContentDescription("Favourite " + (index + 1) + ": " + (app == null ? "Choose an app" : app.label)
                + ". Hold to change.");
        tile.setOnClickListener(v -> {
            AppEntry current = ctx.favouriteApp(index);
            if (current == null) { ctx.navigator.drawer(index); } else { ctx.launch(current); }
        });
        tile.setOnLongClickListener(v -> { Dialogs.favouriteMenu(ctx, index); return true; });
        Motion.pressable(ui, tile);
    }

    private String title(int index, AppEntry app) {
        if (app != null) { return app.label; }
        return ctx.favourites.get(index).isEmpty() ? Slot.at(index).title : "Unavailable";
    }

    private TextView subtitle(int index, AppEntry app) {
        TextView sub = ui.text(app == null ? "Tap to choose" : Slot.at(index).hint, 10, ui.theme.dim);
        oneLine(sub);
        return sub;
    }

    private int glyphColor(Slot slot) { return slot.accented() ? ui.theme.accent : ui.theme.ink; }

    private static void oneLine(TextView text) {
        text.setSingleLine(true);
        text.setEllipsize(TextUtils.TruncateAt.END);
    }

    private LinearLayout.LayoutParams topMargin(int dp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH, WRAP);
        params.topMargin = ui.dp(dp);
        return params;
    }
}
