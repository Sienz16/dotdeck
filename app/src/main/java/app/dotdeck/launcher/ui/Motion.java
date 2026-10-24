package app.dotdeck.launcher.ui;

import android.content.Context;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.util.List;

/** The launcher's few one-shot animations. Nothing here loops or runs while the screen is idle. */
final class Motion {
    private static final long ENTER_MS = 300;
    private static final long ENTER_STAGGER_MS = 45;

    private Motion() { }

    /** False when the user turned system animations off (Developer options or accessibility). */
    static boolean systemAllows(Context context) {
        return Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
    }

    /** Fade and rise in, one after another. */
    static void enter(Ui ui, List<View> views) {
        if (!ui.motion) { return; }
        int index = 0;
        for (View view : views) {
            view.setAlpha(0f);
            view.setTranslationY(ui.dp(14));
            view.animate().alpha(1f).translationY(0f).setDuration(ENTER_MS)
                    .setStartDelay(index++ * ENTER_STAGGER_MS).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    /** Squeeze slightly while pressed. Does not consume the touch, so clicks and long-presses still work. */
    static void pressable(Ui ui, View view) {
        if (!ui.motion) { return; }
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: v.animate().scaleX(.96f).scaleY(.96f).setDuration(90).start(); break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: v.animate().scaleX(1f).scaleY(1f).setDuration(140).start(); break;
                default: break;
            }
            return false;
        });
    }
}
