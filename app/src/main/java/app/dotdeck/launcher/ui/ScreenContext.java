package app.dotdeck.launcher.ui;

import app.dotdeck.launcher.core.AppEntry;
import app.dotdeck.launcher.core.NavState;
import app.dotdeck.launcher.data.AppRepository;
import app.dotdeck.launcher.data.DeckSettings;
import app.dotdeck.launcher.data.FavouriteStore;
import app.dotdeck.launcher.data.IconLoader;

/** What every screen needs, handed over once per render. */
public final class ScreenContext {
    final Ui ui;
    final DeckSettings settings;
    final FavouriteStore favourites;
    final AppRepository repository;
    final IconLoader icons;
    final NavState nav;
    final SystemActions actions;
    final Navigator navigator;
    /** The page was just opened by the user (not redrawn for a data or setting change): play the entrance. */
    final boolean animateIn;

    public ScreenContext(Ui ui, DeckSettings settings, FavouriteStore favourites, AppRepository repository,
            IconLoader icons, NavState nav, SystemActions actions, Navigator navigator, boolean animateIn) {
        this.animateIn = animateIn;
        this.ui = ui;
        this.settings = settings;
        this.favourites = favourites;
        this.repository = repository;
        this.icons = icons;
        this.nav = nav;
        this.actions = actions;
        this.navigator = navigator;
    }

    /** The screen for the current page. */
    public Screen createScreen() {
        switch (nav.page()) {
            case DRAWER: return new DrawerScreen(this);
            case SETTINGS: return new SettingsScreen(this);
            default: return new HomeScreen(this);
        }
    }

    /** The installed app on a tile, or null when the tile is empty or its app is gone. */
    AppEntry favouriteApp(int slot) { return repository.catalog().find(favourites.get(slot)); }

    void launch(AppEntry app) {
        if (!actions.launch(app)) { repository.reload(); }
    }

    void assignAndGoHome(int slot, String key) {
        favourites.assign(slot, key);
        navigator.home();
    }

    void clearAndGoHome(int slot) {
        favourites.clear(slot);
        navigator.home();
    }
}
