package app.dotdeck.launcher.core;

/** Which page is showing, and the drawer's picking slot and search text. Pure state, no views. */
public final class NavState {
    /** Drawer is browsing, not choosing an app for a tile. */
    public static final int BROWSE = -1;

    private Page page = Page.HOME;
    private int picking = BROWSE;
    private String query = "";

    /** Rebuilds state from saved values, discarding anything inconsistent. */
    public static NavState restore(int pageOrdinal, int picking, String query) {
        NavState state = new NavState();
        Page page = Page.fromOrdinal(pageOrdinal);
        if (page == Page.DRAWER) {
            state.showDrawer(picking);
            state.query = query == null ? "" : query;
        } else {
            state.page = page;
        }
        return state;
    }

    public Page page() { return page; }
    public int picking() { return picking; }
    public boolean isPicking() { return picking != BROWSE; }
    public String query() { return query; }

    public void setQuery(String value) { query = value == null ? "" : value; }

    public void showHome() { show(Page.HOME, BROWSE); }

    public void showSettings() { show(Page.SETTINGS, BROWSE); }

    /** @param slot the tile being filled, or BROWSE (any out-of-range value counts as BROWSE) */
    public void showDrawer(int slot) { show(Page.DRAWER, slot >= 0 && slot < FavoriteSlots.COUNT ? slot : BROWSE); }

    private void show(Page target, int slot) {
        page = target;
        picking = slot;
        query = "";
    }
}
