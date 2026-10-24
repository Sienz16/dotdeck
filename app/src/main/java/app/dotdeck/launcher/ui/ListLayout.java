package app.dotdeck.launcher.ui;

import android.widget.LinearLayout;

import static app.dotdeck.launcher.ui.Ui.MATCH;

/** Clock on one side, the six favourites as a single column of wide rows on the other. */
final class ListLayout implements HomeLayout {
    @Override public void build(LinearLayout body, Ui ui, ClockPanel clock, TileFactory tiles) {
        LinearLayout.LayoutParams clockParams;
        LinearLayout.LayoutParams listParams;
        if (ui.portrait) {
            body.setOrientation(LinearLayout.VERTICAL);
            clockParams = new LinearLayout.LayoutParams(MATCH, 0, .75f);
            listParams = new LinearLayout.LayoutParams(MATCH, 0, 1.6f);
        } else {
            body.setOrientation(LinearLayout.HORIZONTAL);
            clockParams = new LinearLayout.LayoutParams(0, MATCH, 1f);
            clockParams.rightMargin = ui.dp(28);
            listParams = new LinearLayout.LayoutParams(0, MATCH, 1.2f);
        }
        int intro = ui.portrait ? ClockPanel.TAGLINE : ClockPanel.FULL;
        body.addView(clock.build(intro, ui.compact ? 74 : 102), clockParams);
        body.addView(TileGrid.of(ui, tiles.all(TileFactory.Style.ROW), TileGrid.ones(6), 1, 8), listParams);
    }
}
