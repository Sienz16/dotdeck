package app.dotdeck.launcher.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import app.dotdeck.launcher.core.Theme;

/**
 * Original dot glyphs. No font assets. Changed digits sweep to their new value in a single 0.5 s
 * animation when the minute rolls over; at rest nothing is scheduled.
 */
public final class DotClock extends View {
    private static final String[] DIGITS = {
        "01110100011000110001100011000101110", "00100011000010000100001000010001110",
        "01110100010000100010001000100011111", "11110000010000101110000010000111110",
        "00010001100101010010111110001000010", "11111100001000011110000010000111110",
        "01110100001000011110100011000101110", "11111000010001000100010000100001000",
        "01110100011000101110100011000101110", "01110100011000101111000010000101110"
    };
    private static final long SWEEP_MS = 500;
    private static final String BLANK = "     ";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int ink;
    private final int faint;
    private final boolean square;
    private final boolean motion;
    private String time = "00:00";
    private String previous = "00:00";
    private float progress = 1f;
    private ValueAnimator sweep;

    public DotClock(Context context, Theme theme, boolean motion) {
        super(context);
        this.ink = theme.ink;
        this.faint = theme.line;
        this.square = theme.squareDots;
        this.motion = motion;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /** @param animate sweep the digits that changed instead of snapping (ignored when motion is off) */
    public void setTime(String value, boolean animate) {
        setContentDescription("Time " + value);
        if (time.equals(value)) { return; }
        previous = time;
        time = value;
        if (animate) { startSweep(); } else { stopSweep(); invalidate(); }
    }

    /** Light every dot up from dark, once. */
    public void sweepIn() {
        previous = BLANK;
        startSweep();
    }

    @Override protected void onDetachedFromWindow() {
        stopSweep();
        super.onDetachedFromWindow();
    }

    private void startSweep() {
        stopSweep();
        if (!motion) { progress = 1f; invalidate(); return; }
        progress = 0f;
        sweep = ValueAnimator.ofFloat(0f, 1f);
        sweep.setDuration(SWEEP_MS);
        sweep.addUpdateListener(animation -> {
            progress = (Float) animation.getAnimatedValue();
            invalidate();
        });
        sweep.start();
    }

    private void stopSweep() {
        if (sweep != null) { sweep.cancel(); sweep = null; }
        progress = 1f;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float step = Math.min(getWidth() / 25f, getHeight() / 8f);
        float left = (getWidth() - step * 25f) / 2f + step / 2f;
        float top = (getHeight() - step * 7f) / 2f + step / 2f;
        int column = 0;
        for (int i = 0; i < time.length(); i++) {
            char c = time.charAt(i);
            if (c == ':') {
                drawDot(canvas, left + column * step, top + 2 * step, step, ink);
                drawDot(canvas, left + column * step, top + 4 * step, step, ink);
                column += 2;
                continue;
            }
            if (c < '0' || c > '9') { continue; }
            char old = i < previous.length() ? previous.charAt(i) : ' ';
            drawDigit(canvas, left, top, step, column, c, progress < 1f && old != c ? old : c);
            column += 6;
        }
    }

    /** Draws digit {@code c}; while it is changing, each dot fades from what {@code from} showed there. */
    private void drawDigit(Canvas canvas, float left, float top, float step, int column, char c, char from) {
        String glyph = DIGITS[c - '0'];
        String old = from >= '0' && from <= '9' ? DIGITS[from - '0'] : null;
        boolean changing = from != c;
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 5; col++) {
                int index = row * 5 + col;
                int target = glyph.charAt(index) == '1' ? ink : faint;
                int color = target;
                if (changing) {
                    int start = old != null && old.charAt(index) == '1' ? ink : faint;
                    float wave = Math.max(0f, Math.min(1f, progress * 1.6f - (col + row * .3f) * .12f));
                    color = blend(start, target, wave);
                }
                drawDot(canvas, left + (column + col) * step, top + row * step, step, color);
            }
        }
    }

    private void drawDot(Canvas canvas, float cx, float cy, float step, int color) {
        paint.setColor(color);
        float size = step * .3f;
        if (square) { canvas.drawRect(cx - size, cy - size, cx + size, cy + size, paint); }
        else { canvas.drawCircle(cx, cy, size, paint); }
    }

    private static int blend(int from, int to, float amount) {
        int a = channel(from, to, amount, 24), r = channel(from, to, amount, 16);
        int g = channel(from, to, amount, 8), b = channel(from, to, amount, 0);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int channel(int from, int to, float amount, int shift) {
        int start = (from >>> shift) & 0xFF;
        int end = (to >>> shift) & 0xFF;
        return Math.round(start + (end - start) * amount);
    }
}
