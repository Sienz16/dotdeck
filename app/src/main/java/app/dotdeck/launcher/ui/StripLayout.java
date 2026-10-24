package app.dotdeck.launcher.ui;

import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import static app.dotdeck.launcher.ui.Ui.MATCH;
import static app.dotdeck.launcher.ui.Ui.WRAP;

/** A large clock, and the favourites as a row of cards you can swipe sideways. */
final class StripLayout implements HomeLayout {
    @Override public void build(LinearLayout body, Ui ui, ClockPanel clock, TileFactory tiles) {
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(clock.build(ClockPanel.TAGLINE, ClockPanel.FILL), new LinearLayout.LayoutParams(MATCH, 0, 1));

        LinearLayout strip = ui.horizontal();
        int cardWidth = ui.dp(ui.portrait ? 150 : ui.compact ? 190 : 230);
        for (View card : tiles.all(TileFactory.Style.CARD)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, MATCH);
            params.rightMargin = ui.dp(10);
            strip.addView(card, params);
        }
        HorizontalScrollView scroll = new HorizontalScrollView(ui.context);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(strip, new HorizontalScrollView.LayoutParams(WRAP, MATCH));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(MATCH, ui.dp(ui.compact ? 112 : 150));
        scrollParams.topMargin = ui.dp(14);
        body.addView(scroll, scrollParams);
    }
}
