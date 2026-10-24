package app.dotdeck.launcher.ui;

import android.widget.LinearLayout;

import static app.dotdeck.launcher.ui.Ui.MATCH;

/** An oversized clock filling the screen, and one dock of round buttons underneath. */
final class DockLayout implements HomeLayout {
    private static final int GAP_DP = 6;

    @Override public void build(LinearLayout body, Ui ui, ClockPanel clock, TileFactory tiles) {
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(clock.build(ClockPanel.BARE, ClockPanel.FILL), new LinearLayout.LayoutParams(MATCH, 0, 1));
        int columns = ui.portrait ? 3 : 6;
        int rows = 6 / columns;
        int rowHeight = ui.dp(ui.compact ? 92 : 112);
        LinearLayout.LayoutParams dockParams = new LinearLayout.LayoutParams(MATCH, rows * rowHeight + (rows - 1) * ui.dp(GAP_DP));
        dockParams.topMargin = ui.dp(12);
        body.addView(TileGrid.of(ui, tiles.all(TileFactory.Style.CHIP), TileGrid.ones(6), columns, GAP_DP), dockParams);
    }
}
