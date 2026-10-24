package app.dotdeck.launcher.data;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import app.dotdeck.launcher.core.AppCatalog;
import app.dotdeck.launcher.core.AppEntry;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Scans installed launcher activities off the main thread. All public methods and the listener
 * run on the main thread. Reloads requested mid-scan are coalesced into one follow-up scan.
 */
public final class AppRepository {
    public interface Listener {
        /** @param success false when the scan failed; the previous catalog stays in place */
        void onCatalogReloaded(boolean success);
    }

    private final PackageManager packages;
    private final String ownPackage;
    private final Handler main;
    private final Listener listener;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private AppCatalog catalog = AppCatalog.EMPTY;
    private boolean loading;
    private boolean reloadPending;
    private boolean closed;

    public AppRepository(PackageManager packages, String ownPackage, Handler main, Listener listener) {
        this.packages = packages;
        this.ownPackage = ownPackage;
        this.main = main;
        this.listener = listener;
    }

    public AppCatalog catalog() { return catalog; }

    public boolean isLoading() { return loading; }

    public void reload() {
        if (closed) { return; }
        if (loading) { reloadPending = true; return; }
        loading = true;
        worker.execute(() -> {
            AppCatalog found = null;
            try { found = scan(); } catch (RuntimeException failure) { /* reported below as a failed reload */ }
            if (Thread.currentThread().isInterrupted()) { return; }
            final AppCatalog result = found;
            main.post(() -> finish(result));
        });
    }

    public void shutdown() {
        closed = true;
        worker.shutdownNow();
    }

    private void finish(AppCatalog result) {
        if (closed) { return; }
        loading = false;
        if (result != null) { catalog = result; }
        listener.onCatalogReloaded(result != null);
        if (reloadPending) { reloadPending = false; reload(); }
    }

    private AppCatalog scan() {
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<AppEntry> found = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo resolved : packages.queryIntentActivities(launcher, 0)) {
            if (Thread.currentThread().isInterrupted()) { break; }
            if (resolved.activityInfo == null || !resolved.activityInfo.exported
                    || ownPackage.equals(resolved.activityInfo.packageName)) { continue; }
            String key = new ComponentName(resolved.activityInfo.packageName, resolved.activityInfo.name)
                    .flattenToString();
            if (seen.add(key)) { found.add(new AppEntry(key, resolved.loadLabel(packages).toString())); }
        }
        final Collator collator = Collator.getInstance();
        Collections.sort(found, (a, b) -> {
            int order = collator.compare(a.label, b.label);
            return order == 0 ? a.key.compareTo(b.key) : order;
        });
        return new AppCatalog(found);
    }
}
