package app.dotdeck.launcher.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

/** Package-list and clock broadcasts, registered only while the launcher is on screen. */
public final class SystemEvents {
    public interface Listener {
        void onClockChanged();
        void onAppsChanged();
    }

    private final Context context;
    private final Listener listener;
    private boolean registered;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context ignored, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_DATE_CHANGED.equals(action)) {
                listener.onClockChanged();
            } else {
                listener.onAppsChanged();
            }
        }
    };

    public SystemEvents(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void register() {
        if (registered) { return; }
        IntentFilter packages = new IntentFilter();
        packages.addAction(Intent.ACTION_PACKAGE_ADDED);
        packages.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packages.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packages.addDataScheme("package");
        IntentFilter system = new IntentFilter();
        system.addAction(Intent.ACTION_TIME_CHANGED);
        system.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        system.addAction(Intent.ACTION_DATE_CHANGED);
        system.addAction(Intent.ACTION_USER_UNLOCKED);
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, packages, Context.RECEIVER_NOT_EXPORTED);
            context.registerReceiver(receiver, system, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, packages);
            context.registerReceiver(receiver, system);
        }
        registered = true;
    }

    public void unregister() {
        if (!registered) { return; }
        context.unregisterReceiver(receiver);
        registered = false;
    }
}
