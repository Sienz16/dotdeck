package app.dotdeck.launcher.ui;

import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

import static app.dotdeck.launcher.ui.Ui.MATCH;

/** Packs cells into equal-height rows of {@code columns} weight units. Cells may span several columns. */
final class TileGrid {
    private TileGrid() { }

    static LinearLayout of(Ui ui, List<View> cells, int[] spans, int columns, int gapDp) {
        int gap = ui.dp(gapDp);
        List<LinearLayout> rows = new ArrayList<>();
        LinearLayout row = null;
        int used = 0;
        for (int i = 0; i < cells.size(); i++) {
            int span = spans[i];
            if (row == null || used + span > columns) {
                fillRow(ui, row, used, columns);
                row = ui.horizontal();
                rows.add(row);
                used = 0;
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, MATCH, span);
            if (used + span < columns) { params.rightMargin = gap; }
            row.addView(cells.get(i), params);
            used += span;
        }
        fillRow(ui, row, used, columns);
        LinearLayout grid = ui.vertical();
        for (int i = 0; i < rows.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH, 0, 1);
            if (i < rows.size() - 1) { params.bottomMargin = gap; }
            grid.addView(rows.get(i), params);
        }
        return grid;
    }

    /** Equal spans of one: the common case. */
    static int[] ones(int count) {
        int[] spans = new int[count];
        java.util.Arrays.fill(spans, 1);
        return spans;
    }

    /** Keeps a short last row aligned with the rows above it. */
    private static void fillRow(Ui ui, LinearLayout row, int used, int columns) {
        if (row != null && used < columns) {
            row.addView(new View(ui.context), new LinearLayout.LayoutParams(0, MATCH, columns - used));
        }
    }
}
