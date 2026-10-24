package app.dotdeck.launcher.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.LruCache;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads app icons on a worker thread into a small LRU cache. Main thread only, except the loading
 * itself. Apps whose icon cannot be loaded are remembered so binding a row never retries forever.
 */
public final class IconLoader {
    public interface Listener {
        /** One or more icons became available; rebind visible rows. Batched, main thread. */
        void onIconsReady();
    }

    private static final long BATCH_MS = 60;
    private static final int MIN_CACHE_BYTES = 2 * 1024 * 1024;
    private static final int MAX_CACHE_BYTES = 6 * 1024 * 1024;

    private final PackageManager packages;
    private final Handler main;
    private final Listener listener;
    private final int sizePx;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Set<String> loading = new HashSet<>();
    private final Set<String> failed = new HashSet<>();
    private final LruCache<String, Bitmap> cache;
    private boolean notifyScheduled;
    private boolean closed;
    private final Runnable notifyReady = this::notifyListener;

    public IconLoader(PackageManager packages, Handler main, int sizePx, Listener listener) {
        this.packages = packages;
        this.main = main;
        this.sizePx = sizePx;
        this.listener = listener;
        long budget = Runtime.getRuntime().maxMemory() / 32;
        cache = new LruCache<String, Bitmap>((int) Math.max(MIN_CACHE_BYTES, Math.min(MAX_CACHE_BYTES, budget))) {
            @Override protected int sizeOf(String key, Bitmap value) { return value.getByteCount(); }
        };
    }

    /** @return the icon if cached; otherwise null, and a load starts unless it failed before */
    public Bitmap get(String key) {
        Bitmap cached = cache.get(key);
        if (cached == null && !failed.contains(key) && loading.add(key)) { load(key); }
        return cached;
    }

    /** Forget everything, e.g. after the app list changed and icons may have been updated. */
    public void clear() {
        cache.evictAll();
        failed.clear();
    }

    public void trim() { cache.evictAll(); }

    public void shutdown() {
        closed = true;
        worker.shutdownNow();
        cache.evictAll();
    }

    private void load(String key) {
        worker.execute(() -> {
            Bitmap bitmap = null;
            try {
                ComponentName component = ComponentName.unflattenFromString(key);
                if (component != null) {
                    Drawable drawable = packages.getActivityIcon(component);
                    bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
                    drawable.setBounds(0, 0, sizePx, sizePx);
                    drawable.draw(new Canvas(bitmap));
                }
            } catch (PackageManager.NameNotFoundException | RuntimeException removed) {
                bitmap = null; // a package can disappear between discovery and icon loading
            }
            final Bitmap result = bitmap;
            main.post(() -> loaded(key, result));
        });
    }

    private void notifyListener() {
        notifyScheduled = false;
        if (!closed) { listener.onIconsReady(); }
    }

    private void loaded(String key, Bitmap bitmap) {
        if (closed) { return; }
        loading.remove(key);
        if (bitmap == null) { failed.add(key); return; }
        cache.put(key, bitmap);
        if (!notifyScheduled) {
            notifyScheduled = true;
            main.postDelayed(notifyReady, BATCH_MS);
        }
    }
}
