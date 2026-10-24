package app.dotdeck.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.view.View;
import android.widget.LinearLayout;
import app.dotdeck.launcher.core.NavState;
import app.dotdeck.launcher.core.Page;
import app.dotdeck.launcher.core.Theme;
import app.dotdeck.launcher.data.AppRepository;
import app.dotdeck.launcher.data.DeckSettings;
import app.dotdeck.launcher.data.FavouriteStore;
import app.dotdeck.launcher.data.IconLoader;
import app.dotdeck.launcher.data.SystemEvents;
import app.dotdeck.launcher.ui.Navigator;
import app.dotdeck.launcher.ui.Screen;
import app.dotdeck.launcher.ui.ScreenContext;
import app.dotdeck.launcher.ui.SystemActions;
import app.dotdeck.launcher.ui.Ui;

/** Lifecycle and wiring only: owns the services, picks the Screen for the current page, applies the theme. */
public final class HomeActivity extends Activity {
    private static final int ICON_SIZE_DP = 40;
    private static final int ICON_SIZE_MAX_PX = 96;

    private final Handler main = new Handler(Looper.getMainLooper());
    private NavState nav = new NavState();
    private DeckSettings settings;
    private FavouriteStore favourites;
    private AppRepository repository;
    private IconLoader icons;
    private SystemEvents events;
    private SystemActions actions;
    private Screen screen;
    private boolean started;

    private final Navigator navigator = new Navigator() {
        @Override public void home() {
            actions.hideKeyboard();
            nav.showHome();
            render(true);
        }

        @Override public void drawer(int slot) {
            actions.hideKeyboard();
            nav.showDrawer(slot);
            render(true);
        }

        @Override public void settings() {
            nav.showSettings();
            render(true);
        }

        @Override public void rerender() { render(false); }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences preferences = createDeviceProtectedStorageContext()
                .getSharedPreferences("dotdeck", MODE_PRIVATE);
        settings = new DeckSettings(preferences);
        favourites = new FavouriteStore(preferences);
        actions = new SystemActions(this);
        repository = new AppRepository(getPackageManager(), getPackageName(), main, this::onCatalogReloaded);
        int iconPx = Math.min(ICON_SIZE_MAX_PX, Math.round(ICON_SIZE_DP * getResources().getDisplayMetrics().density));
        icons = new IconLoader(getPackageManager(), main, iconPx, this::notifyScreenDataChanged);
        events = new SystemEvents(this, new SystemEvents.Listener() {
            @Override public void onClockChanged() { if (started) { screen.start(); } }
            @Override public void onAppsChanged() { reloadApps(); }
        });
        if (state != null) {
            nav = NavState.restore(state.getInt("page"), state.getInt("picking", NavState.BROWSE),
                    state.getString("query", ""));
        }
        render(state == null);
    }

    @Override protected void onStart() {
        super.onStart();
        started = true;
        events.register();
        reloadApps();
        screen.start();
    }

    @Override protected void onStop() {
        started = false;
        screen.stop();
        events.unregister();
        super.onStop();
    }

    @Override protected void onDestroy() {
        main.removeCallbacksAndMessages(null);
        repository.shutdown();
        icons.shutdown();
        super.onDestroy();
    }

    @Override public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_LOW) { icons.trim(); }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("page", nav.page().ordinal());
        state.putInt("picking", nav.picking());
        state.putString("query", nav.query());
        super.onSaveInstanceState(state);
    }

    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        render(false);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        navigator.home();
    }

    @SuppressWarnings("deprecation")
    @Override public void onBackPressed() {
        if (nav.page() != Page.HOME) { navigator.home(); }
    }

    /** @param animateIn the user just arrived on this page, so let it animate in */
    private void render(boolean animateIn) {
        if (screen != null) { screen.stop(); }
        Theme theme = settings.theme();
        applyWindow(theme);
        Ui ui = new Ui(this, theme, settings.motion());
        screen = new ScreenContext(ui, settings, favourites, repository, icons, nav, actions, navigator, animateIn)
                .createScreen();
        LinearLayout root = ui.createRoot();
        screen.build(root);
        setContentView(root);
        root.requestApplyInsets();
        if (started) { screen.start(); }
    }

    @SuppressWarnings("deprecation")
    private void applyWindow(Theme theme) {
        getWindow().setBackgroundDrawable(new ColorDrawable(theme.bg));
        getWindow().setStatusBarColor(theme.bg);
        getWindow().setNavigationBarColor(theme.bg);
        int flags = 0;
        if (theme.lightBars) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) { flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; }
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void reloadApps() {
        repository.reload();
        notifyScreenDataChanged(); // lets the drawer show its loading state
    }

    private void onCatalogReloaded(boolean success) {
        if (success) {
            icons.clear();
            UserManager users = getSystemService(UserManager.class);
            // Before first unlock the app list is incomplete, so seeding would pick the wrong apps.
            if (users != null && users.isUserUnlocked()) { favourites.seedIfNeeded(repository.catalog()); }
        } else if (started) {
            actions.toast("Could not refresh apps. Try reopening DotDeck.");
        }
        if (!started) { return; }
        if (nav.page() == Page.HOME) { render(false); } else { screen.onDataChanged(); }
    }

    private void notifyScreenDataChanged() {
        if (started && screen != null) { screen.onDataChanged(); }
    }
}
