package app.dotdeck.launcher.ui;

import android.widget.LinearLayout;

/** One page of the launcher. The activity builds a new Screen for every render. */
public interface Screen {
    /** Fills the themed root with this page's views. */
    void build(LinearLayout root);

    /** The page is visible: begin any periodic work. May be called repeatedly. */
    default void start() { }

    /** The page is hidden or about to be replaced: stop periodic work. */
    default void stop() { }

    /** The app list or icons changed underneath the page. */
    default void onDataChanged() { }
}
