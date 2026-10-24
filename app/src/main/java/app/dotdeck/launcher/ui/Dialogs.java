package app.dotdeck.launcher.ui;

import app.dotdeck.launcher.core.AppEntry;
import app.dotdeck.launcher.core.FavoriteSlots;

/** The small choose-from-a-list dialogs. */
final class Dialogs {
    private Dialogs() { }

    /** Hold on a favourite tile. */
    static void favouriteMenu(ScreenContext ctx, int slot) {
        ctx.ui.dialog().setTitle("Favourite " + (slot + 1))
                .setItems(new String[]{"Choose app", "Clear tile"}, (dialog, which) -> {
                    if (which == 0) { ctx.navigator.drawer(slot); }
                    else { ctx.clearAndGoHome(slot); }
                }).show();
    }

    /** Hold on an app in the drawer. */
    static void appOptions(ScreenContext ctx, AppEntry app) {
        ctx.ui.dialog().setTitle(app.label)
                .setItems(new String[]{"Set as favourite", "App info"}, (dialog, which) -> {
                    if (which == 0) { chooseTile(ctx, app); }
                    else { ctx.actions.openAppInfo(app); }
                }).show();
    }

    private static void chooseTile(ScreenContext ctx, AppEntry app) {
        String[] labels = new String[FavoriteSlots.COUNT];
        for (int i = 0; i < labels.length; i++) {
            AppEntry current = ctx.favouriteApp(i);
            labels[i] = "0" + (i + 1) + " / " + (current == null ? "Empty" : current.label);
        }
        ctx.ui.dialog().setTitle("Choose a tile")
                .setItems(labels, (picker, slot) -> ctx.assignAndGoHome(slot, app.key)).show();
    }
}
