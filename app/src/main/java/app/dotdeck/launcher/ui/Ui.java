package app.dotdeck.launcher.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.dotdeck.launcher.core.Glyph;
import app.dotdeck.launcher.core.Theme;

/** Screen metrics plus the view factory for one theme. Built fresh on every render. */
public final class Ui {
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    public final Context context;
    public final Theme theme;
    public final boolean portrait;
    public final boolean compact;
    /** One-shot animations are on: the user wants them and the system allows them. */
    public final boolean motion;
    private final float density;

    public Ui(Context context, Theme theme, boolean motionWanted) {
        this.context = context;
        this.theme = theme;
        this.motion = motionWanted && Motion.systemAllows(context);
        Configuration config = context.getResources().getConfiguration();
        portrait = config.screenHeightDp > config.screenWidthDp;
        compact = config.screenHeightDp < 480 || (portrait && config.screenHeightDp < 760);
        density = context.getResources().getDisplayMetrics().density;
    }

    public int dp(float value) { return Math.round(value * density); }

    /** The page root: themed background, padding, and system-bar insets folded into the padding. */
    public LinearLayout createRoot() {
        LinearLayout root = vertical();
        root.setBackgroundColor(theme.bg);
        final int side = dp(compact ? 18 : 28);
        final int edge = dp(12);
        root.setPadding(side, edge, side, edge);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(side + insets.getSystemWindowInsetLeft(), edge + insets.getSystemWindowInsetTop(),
                    side + insets.getSystemWindowInsetRight(), edge + insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        return root;
    }

    public LinearLayout vertical() {
        LinearLayout result = new LinearLayout(context);
        result.setOrientation(LinearLayout.VERTICAL);
        return result;
    }

    public LinearLayout horizontal() {
        LinearLayout result = new LinearLayout(context);
        result.setOrientation(LinearLayout.HORIZONTAL);
        result.setGravity(Gravity.CENTER_VERTICAL);
        return result;
    }

    public TextView text(String value, float size, int color) {
        TextView result = new TextView(context);
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(color);
        result.setIncludeFontPadding(false);
        return result;
    }

    public TextView mono(String value, float size, int color) {
        TextView result = text(value, size, color);
        result.setTypeface(Typeface.MONOSPACE);
        return result;
    }

    public TextView headline(String value, float size) {
        TextView result = text(value, size, theme.ink);
        result.setTypeface(theme.monoHeadline ? Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                : Typeface.create("sans-serif", Typeface.BOLD));
        return result;
    }

    /** A rounded rectangle; radius is in dp and scaled by the theme's corner multiplier. */
    public GradientDrawable shape(int color, float radius, boolean bordered) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(color);
        result.setCornerRadius(dp(radius * theme.radius));
        if (bordered) { result.setStroke(Math.max(1, dp(1)), theme.line); }
        return result;
    }

    /** Tile and row background: the panel colour, with a hairline border on outlined themes. */
    public Drawable panel(float radius) { return ripple(shape(theme.panel, radius, theme.outlined)); }

    public Drawable solid(int color, float radius) { return ripple(shape(color, radius, false)); }

    /** Background for a round glyph button of the given pixel size; square on hard-edged themes. */
    public Drawable chip(int sizePx) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(theme.panel);
        result.setCornerRadius(sizePx / 2f * Math.min(1f, theme.radius * 2f));
        if (theme.outlined) { result.setStroke(Math.max(1, dp(1)), theme.line); }
        return ripple(result);
    }

    private Drawable ripple(GradientDrawable content) {
        return new RippleDrawable(ColorStateList.valueOf(theme.lightBars ? 0x22000000 : 0x33FFFFFF), content, null);
    }

    public View button(String label, Glyph glyph, boolean accent, View.OnClickListener action) {
        int ink = accent ? theme.onAccent : theme.ink;
        LinearLayout button = horizontal();
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setBackground(accent ? solid(theme.accent, 28) : panel(28));
        button.addView(new GlyphView(context, glyph, ink), new LinearLayout.LayoutParams(dp(20), dp(20)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP, WRAP);
        params.leftMargin = dp(10);
        button.addView(mono(label, 11, ink), params);
        button.setContentDescription(label);
        button.setOnClickListener(action);
        Motion.pressable(this, button);
        return button;
    }

    /** Back button plus title block, shared by the drawer and settings pages. */
    public void addPageHeader(LinearLayout root, String title, String subtitle, View.OnClickListener back) {
        LinearLayout header = horizontal();
        header.addView(button("HOME", Glyph.BACK, false, back), new LinearLayout.LayoutParams(dp(112), dp(48)));
        LinearLayout copy = vertical();
        copy.setPadding(dp(20), 0, 0, 0);
        copy.addView(headline(title, compact ? 22 : 28));
        copy.addView(mono(subtitle, 9, theme.dim));
        header.addView(copy, new LinearLayout.LayoutParams(0, WRAP, 1));
        root.addView(header, new LinearLayout.LayoutParams(MATCH, dp(compact ? 56 : 72)));
    }

    public AlertDialog.Builder dialog() {
        return new AlertDialog.Builder(context, theme.lightBars
                ? android.R.style.Theme_Material_Light_Dialog_Alert : android.R.style.Theme_Material_Dialog_Alert);
    }
}
