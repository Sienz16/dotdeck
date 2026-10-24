package app.dotdeck.launcher.ui;

/** How a screen asks the activity to change page or redraw. */
public interface Navigator {
    void home();

    /** @param slot the tile to fill, or NavState.BROWSE */
    void drawer(int slot);

    void settings();

    /** Redraw the current page, e.g. after a setting or theme changed. */
    void rerender();
}
