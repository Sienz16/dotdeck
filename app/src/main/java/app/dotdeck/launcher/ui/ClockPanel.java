package app.dotdeck.launcher.ui;

import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.dotdeck.launcher.core.Theme;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static app.dotdeck.launcher.ui.Ui.MATCH;
import static app.dotdeck.launcher.ui.Ui.WRAP;

/** The dot clock with its date line and optional tagline and headline. One per home screen. */
final class ClockPanel {
    /** Tagline and headline above the clock. */
    static final int FULL = 0;
    /** Tagline only. */
    static final int TAGLINE = 1;
    /** Just clock and date. */
    static final int BARE = 2;
    /** Pass as the clock height to let it fill the panel. */
    static final int FILL = 0;

    private final ScreenContext ctx;
    private final Ui ui;
    private DotClock clock;
    private TextView date;
    private LinearLayout view;

    ClockPanel(ScreenContext ctx) {
        this.ctx = ctx;
        this.ui = ctx.ui;
    }

    /** @return the panel; its height is the caller's to set (weight, or fixed) */
    LinearLayout build(int intro, int clockHeightDp) {
        Theme theme = ui.theme;
        view = ui.vertical();
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (intro != BARE) {
            view.addView(ui.mono("A LITTLE LESS. A LOT FURTHER.", ui.compact ? 9 : 10, theme.dim));
        }
        if (intro == FULL) {
            TextView headline = ui.headline(ui.portrait ? "Your road." : "Your road.\nYour rhythm.", ui.compact ? 25 : 35);
            headline.setLineSpacing(0, 1.02f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH, WRAP);
            params.topMargin = ui.dp(10);
            params.bottomMargin = ui.dp(10);
            view.addView(headline, params);
        }
        clock = new DotClock(ui.context, theme, ui.motion);
        clock.setOnClickListener(v -> ctx.actions.openSystem(Settings.ACTION_DATE_SETTINGS));
        clock.setFocusable(true);
        LinearLayout.LayoutParams clockParams = clockHeightDp == FILL
                ? new LinearLayout.LayoutParams(MATCH, 0, 1)
                : new LinearLayout.LayoutParams(MATCH, ui.dp(clockHeightDp));
        if (intro == TAGLINE) { clockParams.topMargin = ui.dp(10); }
        view.addView(clock, clockParams);
        date = ui.mono("", ui.compact ? 11 : 12, theme.dim);
        date.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(MATCH, WRAP);
        dateParams.topMargin = ui.dp(10);
        view.addView(date, dateParams);
        return view;
    }

    LinearLayout view() { return view; }

    /** Light the dots up from dark, once. Used when a page opens. */
    void playIntro() { if (clock != null) { clock.sweepIn(); } }

    /** @param animate sweep changed digits instead of snapping */
    void update(boolean animate) {
        if (clock == null) { return; }
        Date now = new Date();
        boolean clock24 = ctx.settings.clock24();
        clock.setTime(new SimpleDateFormat(clock24 ? "HH:mm" : "hh:mm", Locale.US).format(now), animate);
        String value = DateFormat.getMediumDateFormat(ui.context).format(now);
        String day = new SimpleDateFormat("EEE", Locale.getDefault()).format(now);
        String period = clock24 ? "" : " / " + new SimpleDateFormat("a", Locale.getDefault()).format(now);
        date.setText((day + " / " + value + period).toUpperCase(Locale.getDefault()));
    }
}
