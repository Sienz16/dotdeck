package app.dotdeck.launcher.ui;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.dotdeck.launcher.core.Glyph;
import app.dotdeck.launcher.core.NavState;
import app.dotdeck.launcher.core.Theme;
import app.dotdeck.launcher.core.Themes;
import java.util.ArrayList;
import java.util.List;

import static app.dotdeck.launcher.ui.Ui.MATCH;
import static app.dotdeck.launcher.ui.Ui.WRAP;

/** Header, footer and the minute clock tick. The middle is whatever {@link HomeLayout} the user picked. */
final class HomeScreen implements Screen {
    private final ScreenContext ctx;
    private final Ui ui;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ClockPanel clock;
    private final TileFactory tiles;
    private boolean ticked;
    private boolean introPending;

    private final Runnable minuteTick = new Runnable() {
        @Override public void run() {
            clock.update(ticked);
            ticked = true;
            main.postDelayed(this, 60000 - System.currentTimeMillis() % 60000);
        }
    };

    HomeScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.ui = ctx.ui;
        this.clock = new ClockPanel(ctx);
        this.tiles = new TileFactory(ctx);
    }

    @Override public void start() {
        stop();
        minuteTick.run();
        if (introPending) {
            introPending = false;
            clock.playIntro();
        }
    }

    @Override public void stop() { main.removeCallbacks(minuteTick); }

    @Override public void build(LinearLayout root) {
        Theme theme = ui.theme;
        LinearLayout header = ui.horizontal();
        header.addView(ui.mono("D O T D E C K", 14, theme.ink), new LinearLayout.LayoutParams(0, WRAP, 1));
        header.addView(ui.mono(Themes.label(theme), 10, theme.dim));
        root.addView(header, new LinearLayout.LayoutParams(MATCH, ui.dp(36)));
        View rule = new View(ui.context);
        rule.setBackgroundColor(theme.line);
        root.addView(rule, new LinearLayout.LayoutParams(MATCH, ui.dp(1)));

        LinearLayout body = ui.vertical();
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(MATCH, 0, 1);
        bodyParams.topMargin = ui.dp(ui.compact ? 14 : 24);
        bodyParams.bottomMargin = ui.dp(ui.compact ? 14 : 24);
        root.addView(body, bodyParams);
        HomeLayouts.create(ctx.settings.layout()).build(body, ui, clock, tiles);

        root.addView(buildFooter(), new LinearLayout.LayoutParams(MATCH, ui.dp(48)));

        if (ctx.animateIn && ui.motion) {
            List<View> entering = new ArrayList<>(tiles.created());
            entering.add(clock.view());
            Motion.enter(ui, entering);
            introPending = true;
        }
    }

    private LinearLayout buildFooter() {
        Theme theme = ui.theme;
        LinearLayout footer = ui.horizontal();
        TextView hint = ui.mono(ui.portrait ? "" : "HOLD A TILE TO MAKE IT YOURS", 9, theme.dim);
        hint.setMaxLines(2);
        footer.addView(hint, new LinearLayout.LayoutParams(0, MATCH, 1));
        View settings = ui.button(ui.portrait ? "" : "SETTINGS", Glyph.SETTINGS, false, v -> ctx.navigator.settings());
        settings.setContentDescription("SETTINGS");
        footer.addView(settings, new LinearLayout.LayoutParams(ui.dp(ui.portrait ? 48 : ui.compact ? 126 : 144), ui.dp(48)));
        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(ui.dp(ui.compact ? 142 : 164), ui.dp(48));
        allParams.leftMargin = ui.dp(10);
        footer.addView(ui.button("ALL APPS", Glyph.APPS, true, v -> ctx.navigator.drawer(NavState.BROWSE)), allParams);
        return footer;
    }
}
