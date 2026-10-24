package app.dotdeck.launcher.ui;

import android.widget.LinearLayout;

import static app.dotdeck.launcher.ui.Ui.MATCH;

/** Clock and headline on one side, a 3 x 2 grid of cards on the other. */
final class DeckLayout implements HomeLayout {
    @Override public void build(LinearLayout body, Ui ui, ClockPanel clock, TileFactory tiles) {
        body.setOrientation(ui.portrait ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams introParams;
        LinearLayout.LayoutParams gridParams;
        if (ui.portrait) {
            introParams = new LinearLayout.LayoutParams(MATCH, 0, .8f);
            gridParams = new LinearLayout.LayoutParams(MATCH, 0, 1.2f);
        } else {
            introParams = new LinearLayout.LayoutParams(0, MATCH, .9f);
            introParams.rightMargin = ui.dp(28);
            gridParams = new LinearLayout.LayoutParams(0, MATCH, 1.5f);
        }
        body.addView(clock.build(ClockPanel.FULL, ui.compact ? 74 : 102), introParams);
        body.addView(TileGrid.of(ui, tiles.all(TileFactory.Style.CARD), TileGrid.ones(6), 3, 10), gridParams);
    }
}
