package app.dotdeck.launcher.ui;

import android.widget.LinearLayout;

/** Arranges the clock and the six favourite tiles inside the home body. */
interface HomeLayout {
    /** @param body an empty vertical container that fills the space between header and footer */
    void build(LinearLayout body, Ui ui, ClockPanel clock, TileFactory tiles);
}
