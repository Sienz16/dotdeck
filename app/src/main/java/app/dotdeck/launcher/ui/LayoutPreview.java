package app.dotdeck.launcher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import app.dotdeck.launcher.core.LayoutStyle;
import app.dotdeck.launcher.core.Theme;

/** Wireframe of a home layout, drawn in the current theme's colours. */
final class LayoutPreview extends View {
    private final LayoutStyle style;
    private final Theme theme;
    private final boolean selected;
    private final int selectionColor;
    private final float density;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();

    LayoutPreview(Context context, LayoutStyle style, Theme theme, boolean selected, int selectionColor) {
        super(context);
        this.style = style;
        this.theme = theme;
        this.selected = selected;
        this.selectionColor = selectionColor;
        this.density = context.getResources().getDisplayMetrics().density;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = (selected ? 2.5f : 1f) * density;
        float corner = 12 * density * Math.max(theme.radius, .35f);
        box.set(stroke / 2, stroke / 2, getWidth() - stroke / 2, getHeight() - stroke / 2);
        fill(canvas, box, corner, theme.bg);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(selected ? selectionColor : theme.line);
        canvas.drawRoundRect(box, corner, corner, paint);

        float pad = 10 * density;
        float l = pad, t = pad, r = getWidth() - pad, b = getHeight() - pad;
        canvas.save();
        canvas.clipRect(l, t, r, b);
        switch (style) {
            case DOCK: drawDock(canvas, l, t, r, b); break;
            case LIST: drawList(canvas, l, t, r, b); break;
            case BENTO: drawBento(canvas, l, t, r, b); break;
            case STRIP: drawStrip(canvas, l, t, r, b); break;
            default: drawDeck(canvas, l, t, r, b); break;
        }
        canvas.restore();
    }

    private void drawDeck(Canvas canvas, float l, float t, float r, float b) {
        float split = l + (r - l) * .38f;
        clock(canvas, l, (t + b) / 2 - 5 * density, split - l);
        grid(canvas, split + 5 * density, t, r, b, 3, 2);
    }

    private void drawDock(Canvas canvas, float l, float t, float r, float b) {
        clock(canvas, l + (r - l) * .15f, t + (b - t) * .22f, (r - l) * .7f);
        float radius = Math.min((r - l) / 14f, (b - t) * .14f);
        for (int i = 0; i < 6; i++) {
            float cx = l + (r - l) * (i + .5f) / 6f;
            float cy = b - radius;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(theme.panel);
            if (theme.radius == 0f) { canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, paint); }
            else { canvas.drawCircle(cx, cy, radius, paint); }
            dot(canvas, cx, cy, 1.8f * density, i == 0 ? theme.accent : theme.ink);
        }
    }

    private void drawList(Canvas canvas, float l, float t, float r, float b) {
        float split = l + (r - l) * .4f;
        clock(canvas, l, (t + b) / 2 - 5 * density, split - l);
        float gap = 3 * density;
        float rowHeight = ((b - t) - 5 * gap) / 6f;
        for (int i = 0; i < 6; i++) {
            float top = t + i * (rowHeight + gap);
            tile(canvas, split + 5 * density, top, r, top + rowHeight, i == 0);
        }
    }

    private void drawBento(Canvas canvas, float l, float t, float r, float b) {
        float gap = 4 * density;
        float cell = ((r - l) - 3 * gap) / 4f;
        float height = ((b - t) - gap) / 2f;
        tile(canvas, l, t, l + 2 * cell + gap, t + height, false);
        clock(canvas, l + 6 * density, t + height / 2 - 5 * density, 2 * cell - 12 * density);
        for (int i = 0; i < 6; i++) {
            int slot = i + 2;
            float left = l + (slot % 4) * (cell + gap);
            float top = t + (slot / 4) * (height + gap);
            tile(canvas, left, top, left + cell, top + height, i == 0);
        }
    }

    private void drawStrip(Canvas canvas, float l, float t, float r, float b) {
        clock(canvas, l + (r - l) * .15f, t + (b - t) * .12f, (r - l) * .7f);
        float gap = 4 * density;
        float card = (r - l) * .34f;
        float top = t + (b - t) * .58f;
        for (int i = 0; i < 4; i++) {
            float left = l + i * (card + gap);
            tile(canvas, left, top, left + card, b, i == 0);
        }
    }

    /** Two rows of dots standing in for the clock, spread over the given width. */
    private void clock(Canvas canvas, float x, float y, float width) {
        float radius = Math.max(1.5f * density, Math.min(2.6f * density, width / 22f));
        int count = Math.max(3, Math.min(9, (int) (width / (radius * 3.4f))));
        for (int row = 0; row < 2; row++) {
            for (int i = 0; i < count; i++) {
                dot(canvas, x + radius + i * (width - 2 * radius) / (count - 1), y + radius + row * radius * 3.2f,
                        radius, (i + row) % 3 == 2 ? theme.line : theme.ink);
            }
        }
    }

    private void grid(Canvas canvas, float l, float t, float r, float b, int columns, int rows) {
        float gap = 3 * density;
        float width = ((r - l) - (columns - 1) * gap) / columns;
        float height = ((b - t) - (rows - 1) * gap) / rows;
        for (int i = 0; i < columns * rows; i++) {
            float left = l + (i % columns) * (width + gap);
            float top = t + (i / columns) * (height + gap);
            tile(canvas, left, top, left + width, top + height, i == 0);
        }
    }

    private void tile(Canvas canvas, float l, float t, float r, float b, boolean accent) {
        float corner = 5 * density * theme.radius;
        box.set(l, t, r, b);
        fill(canvas, box, corner, theme.panel);
        if (theme.outlined) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(density);
            paint.setColor(theme.line);
            canvas.drawRoundRect(box, corner, corner, paint);
        }
        dot(canvas, l + 5 * density, t + 5 * density, 1.7f * density, accent ? theme.accent : theme.ink);
    }

    private void fill(Canvas canvas, RectF rect, float corner, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(rect, corner, corner, paint);
    }

    private void dot(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        if (theme.squareDots) { canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, paint); }
        else { canvas.drawCircle(cx, cy, radius, paint); }
    }
}
