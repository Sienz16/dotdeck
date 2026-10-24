package app.dotdeck.launcher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import app.dotdeck.launcher.core.Glyph;

/** Original lightweight symbols, drawn through Android Canvas. */
public final class GlyphView extends View {
    private final Glyph kind;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path navigationArrow = new Path();

    public GlyphView(Context context, Glyph kind, int color) {
        super(context);
        this.kind = kind;
        paint.setColor(color);
        paint.setStrokeWidth(1.7f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        navigationArrow.moveTo(12, 2);
        navigationArrow.lineTo(21, 21);
        navigationArrow.lineTo(12, 16);
        navigationArrow.lineTo(3, 21);
        navigationArrow.close();
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        float size = Math.min(getWidth(), getHeight());
        canvas.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
        canvas.scale(size / 24f, size / 24f);
        paint.setStyle(Paint.Style.STROKE);
        switch (kind) {
            case APPS:
                paint.setStyle(Paint.Style.FILL);
                for (int x = 5; x <= 19; x += 7) {
                    for (int y = 5; y <= 19; y += 7) { canvas.drawCircle(x, y, 1.6f, paint); }
                }
                break;
            case SETTINGS:
                canvas.drawCircle(12, 12, 4, paint);
                for (int i = 0; i < 8; i++) {
                    canvas.save(); canvas.rotate(i * 45, 12, 12);
                    canvas.drawLine(12, 2, 12, 5, paint); canvas.restore();
                }
                break;
            case BACK:
                canvas.drawLine(18, 12, 5, 12, paint);
                canvas.drawLine(5, 12, 10, 7, paint);
                canvas.drawLine(5, 12, 10, 17, paint);
                break;
            case ARROW:
                canvas.drawLine(5, 19, 18, 6, paint);
                canvas.drawLine(9, 6, 18, 6, paint);
                canvas.drawLine(18, 6, 18, 15, paint);
                break;
            case MUSIC:
                canvas.drawLine(9, 17, 9, 6, paint); canvas.drawLine(9, 6, 19, 3, paint);
                canvas.drawLine(19, 3, 19, 14, paint);
                canvas.drawOval(3, 15, 9, 20, paint); canvas.drawOval(13, 12, 19, 17, paint);
                break;
            case NAV:
                canvas.drawPath(navigationArrow, paint);
                break;
            case PHONE:
                canvas.drawRoundRect(6, 2, 18, 22, 3, 3, paint);
                canvas.drawLine(10, 18, 14, 18, paint);
                break;
            case PROJECTION:
                canvas.drawRoundRect(2, 3, 22, 17, 3, 3, paint);
                canvas.drawLine(8, 22, 16, 22, paint); canvas.drawLine(12, 17, 12, 22, paint);
                break;
            case CAMERA:
                canvas.drawRoundRect(2, 6, 22, 21, 3, 3, paint);
                canvas.drawCircle(12, 13, 4, paint);
                canvas.drawLine(8, 6, 9, 3, paint); canvas.drawLine(9, 3, 15, 3, paint);
                canvas.drawLine(15, 3, 16, 6, paint);
                break;
            case PLUS:
            default:
                canvas.drawLine(5, 12, 19, 12, paint); canvas.drawLine(12, 5, 12, 19, paint);
        }
        canvas.restore();
    }
}
