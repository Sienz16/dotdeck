package app.dotdeck.launcher.ui;

import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

import static app.dotdeck.launcher.ui.Ui.MATCH;

/** One box grid: the clock is a double-width tile, the favourites fill the rest. */
final class BentoLayout implements HomeLayout {
    @Override public void build(LinearLayout body, Ui ui, ClockPanel clock, TileFactory tiles) {
        body.setOrientation(LinearLayout.VERTICAL);
        LinearLayout clockTile = ui.vertical();
        clockTile.setBackground(ui.panel(20));
        clockTile.setPadding(ui.dp(14), ui.dp(ui.compact ? 8 : 14), ui.dp(14), ui.dp(ui.compact ? 8 : 14));
        clockTile.addView(clock.build(ClockPanel.BARE, ClockPanel.FILL), new LinearLayout.LayoutParams(MATCH, MATCH));

        List<View> cells = new ArrayList<>();
        cells.add(clockTile);
        cells.addAll(tiles.all(TileFactory.Style.CARD));
        int columns = ui.portrait ? 2 : 4;
        int[] spans = TileGrid.ones(cells.size());
        spans[0] = 2;
        body.addView(TileGrid.of(ui, cells, spans, columns, 10), new LinearLayout.LayoutParams(MATCH, 0, 1));
    }
}
