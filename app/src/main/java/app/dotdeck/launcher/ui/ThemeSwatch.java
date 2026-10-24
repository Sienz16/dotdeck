package app.dotdeck.launcher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import app.dotdeck.launcher.core.Theme;

/** Miniature of a theme: its background, a row of clock dots, two tiles and the accent. */
final class ThemeSwatch extends View {
    private final Theme theme;
    private final boolean selected;
    private final int selectionColor;
    private final float density;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();

    ThemeSwatch(Context context, Theme theme, boolean selected, int selectionColor) {
        super(context);
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
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.bg);
        canvas.drawRoundRect(box, corner, corner, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(selected ? selectionColor : theme.line);
        canvas.drawRoundRect(box, corner, corner, paint);

        float pad = 9 * density;
        float dot = 2.4f * density;
        float x = pad + dot;
        float y = pad + dot;
        for (int i = 0; i < 6; i++) {
            drawDot(canvas, x + i * dot * 3.2f, y, dot, i < 4 ? theme.ink : theme.line);
        }

        float tileTop = getHeight() * .46f;
        float tileBottom = getHeight() - pad;
        float gap = 5 * density;
        float tileWidth = (getWidth() - 2 * pad - gap) / 2;
        for (int i = 0; i < 2; i++) {
            float left = pad + i * (tileWidth + gap);
            box.set(left, tileTop, left + tileWidth, tileBottom);
            float tileCorner = 6 * density * theme.radius;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(theme.panel);
            canvas.drawRoundRect(box, tileCorner, tileCorner, paint);
            if (theme.outlined) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(density);
                paint.setColor(theme.line);
                canvas.drawRoundRect(box, tileCorner, tileCorner, paint);
            }
            drawDot(canvas, left + 6 * density, tileTop + 6 * density, 2.2f * density, i == 0 ? theme.accent : theme.ink);
        }
    }

    private void drawDot(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        if (theme.squareDots) { canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, paint); }
        else { canvas.drawCircle(cx, cy, radius, paint); }
    }
}
