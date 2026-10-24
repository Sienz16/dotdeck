package app.dotdeck.launcher.ui;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import app.dotdeck.launcher.core.AppEntry;

/** Everything that leaves the launcher: starting apps, opening system settings, keyboard, toasts. */
public final class SystemActions {
    private static final int REQUEST_HOME_ROLE = 1;

    private final Activity activity;

    public SystemActions(Activity activity) { this.activity = activity; }

    /** @return false when the app could not be started (and the user has been told) */
    public boolean launch(AppEntry app) {
        ComponentName component = ComponentName.unflattenFromString(app.key);
        if (component == null) { toast("This app shortcut is unavailable."); return false; }
        hideKeyboard();
        try {
            activity.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                    .setComponent(component)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
            return true;
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            toast("App unavailable. Hold the tile to choose another.");
            return false;
        }
    }

    /** Opens a Settings screen, falling back to the main Settings app, then to a toast. */
    public void openSystem(String action) {
        hideKeyboard();
        try { activity.startActivity(new Intent(action)); }
        catch (ActivityNotFoundException | SecurityException unavailable) {
            if (!Settings.ACTION_SETTINGS.equals(action)) { openSystem(Settings.ACTION_SETTINGS); }
            else { toast("System settings are not available on this player."); }
        }
    }

    public void openAppInfo(AppEntry app) {
        ComponentName component = ComponentName.unflattenFromString(app.key);
        if (component == null) { toast("App info is not available."); return; }
        try {
            activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + component.getPackageName())));
        } catch (ActivityNotFoundException | SecurityException failure) {
            toast("App info is not available.");
        }
    }

    public void chooseHome() {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roles = activity.getSystemService(RoleManager.class);
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME) && !roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                try {
                    activity.startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME), REQUEST_HOME_ROLE);
                    return;
                } catch (ActivityNotFoundException | SecurityException ignored) { /* Firmware may omit the role chooser. */ }
            }
        }
        openSystem(Settings.ACTION_HOME_SETTINGS);
    }

    public void hideKeyboard() {
        View focus = activity.getCurrentFocus();
        InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
        if (focus != null) { focus.clearFocus(); }
    }

    public void toast(String message) { Toast.makeText(activity, message, Toast.LENGTH_SHORT).show(); }
}
