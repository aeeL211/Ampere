package io.github.aeel211.ampere;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Locale;

/**
 * Material 3 inspired dashboard drawn directly on a canvas.
 *
 * <p>No AndroidX, no Material Components, no images. The whole UI is a few
 * thousand draw calls per second at most and keeps the APK tiny.</p>
 */
public final class AmpereView extends View {

    private static final String DASH = "—";
    private static final float CONTENT_HEIGHT_DP = 640f;
    private static final float MIN_VIEW_HEIGHT_DP = 680f;
    private static final float CONTENT_MAX_WIDTH_DP = 480f;
    private static final float SIDE_PADDING_DP = 20f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private final Typeface regular = Typeface.create("sans-serif", Typeface.NORMAL);
    private final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private final Typeface light = Typeface.create("sans-serif-light", Typeface.NORMAL);

    private final int surfaceContainer;
    private final int surfaceContainerHigh;
    private final int onSurface;
    private final int onSurfaceVariant;
    private final int charge;
    private final int drain;
    private final int idle;

    private Reading reading;

    public AmpereView(Context context) {
        super(context);
        setWillNotDraw(false);
        setBackgroundColor(context.getColor(R.color.surface));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        surfaceContainer = context.getColor(R.color.surface_container);
        surfaceContainerHigh = context.getColor(R.color.surface_container_high);
        onSurface = context.getColor(R.color.on_surface);
        onSurfaceVariant = context.getColor(R.color.on_surface_variant);
        charge = context.getColor(R.color.charge);
        drain = context.getColor(R.color.drain);
        idle = context.getColor(R.color.idle);

        reading = new Reading(0L, -1, BatteryManager.BATTERY_STATUS_UNKNOWN, 0, false);
        setContentDescription(context.getString(R.string.app_name));
    }

    public void setReading(Reading newReading) {
        if (newReading == null || newReading.equals(reading)) {
            return;
        }
        reading = newReading;
        describe(reading);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width == 0) {
            width = getResources().getDisplayMetrics().widthPixels;
        }
        int height = Math.max(
                Math.round(dp(MIN_VIEW_HEIGHT_DP)),
                MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float contentTop = (getHeight() - dp(CONTENT_HEIGHT_DP)) / 2f;
        float contentWidth = Math.min(
                getWidth() - dp(SIDE_PADDING_DP) * 2f,
                dp(CONTENT_MAX_WIDTH_DP));
        float left = (getWidth() - contentWidth) / 2f;
        float right = left + contentWidth;

        drawHeader(canvas, contentTop, left, right);
        drawHero(canvas, contentTop, left, right);
        drawCards(canvas, contentTop, left, right);
        drawFooter(canvas, contentTop, left, right);
    }

    private void drawHeader(Canvas canvas, float top, float left, float right) {
        drawText(canvas, getContext().getString(R.string.app_name),
                left, top + dp(30f), sp(28f), onSurface, medium, Paint.Align.LEFT, 0f);
        drawText(canvas, getContext().getString(R.string.tagline),
                left, top + dp(51f), sp(13f), onSurfaceVariant, regular, Paint.Align.LEFT, 0f);
        drawBatteryChip(canvas, top + dp(4f), right);
    }

    private void drawBatteryChip(Canvas canvas, float top, float right) {
        float height = dp(34f);
        float width = dp(88f);
        float left = right - width;

        rect.set(left, top, right, top + height);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(surfaceContainerHigh);
        canvas.drawRoundRect(rect, height / 2f, height / 2f, paint);

        int accent = chipAccent();
        float bodyWidth = dp(17f);
        float bodyHeight = dp(10f);
        float bodyLeft = left + dp(13f);
        float bodyTop = top + (height - bodyHeight) / 2f;

        rect.set(bodyLeft, bodyTop, bodyLeft + bodyWidth, bodyTop + bodyHeight);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.4f));
        paint.setColor(accent);
        canvas.drawRoundRect(rect, dp(3f), dp(3f), paint);

        rect.set(
                bodyLeft + bodyWidth + dp(1f),
                bodyTop + dp(3f),
                bodyLeft + bodyWidth + dp(3f),
                bodyTop + bodyHeight - dp(3f));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, dp(1f), dp(1f), paint);

        int level = reading.level();
        if (level > 0) {
            float inset = dp(2f);
            float fillWidth = (bodyWidth - inset * 2f) * Math.min(level, 100) / 100f;
            rect.set(
                    bodyLeft + inset,
                    bodyTop + inset,
                    bodyLeft + inset + fillWidth,
                    bodyTop + bodyHeight - inset);
            canvas.drawRoundRect(rect, dp(1.5f), dp(1.5f), paint);
        }

        String percent = level >= 0 ? Math.min(level, 100) + "%" : DASH;
        int color = level >= 0 && level <= 15 ? drain : onSurface;
        drawText(canvas, percent, right - dp(14f), top + height / 2f - centerOffset(sp(13f), medium),
                sp(13f), color, medium, Paint.Align.RIGHT, 0f);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0f);
    }

    private void drawHero(Canvas canvas, float top, float left, float right) {
        float heroTop = top + dp(78f);
        float heroBottom = top + dp(458f);

        rect.set(left, heroTop, right, heroBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(surfaceContainer);
        canvas.drawRoundRect(rect, dp(30f), dp(30f), paint);

        drawText(canvas, label(R.string.net_label),
                left + dp(26f), heroTop + dp(34f), sp(12f), onSurfaceVariant,
                medium, Paint.Align.LEFT, 0.12f);

        boolean supported = reading.supported();
        String value = supported ? netValue(reading.netUa()) : DASH;
        int accent = statusAccent();
        int valueColor = supported && reading.netUa() != 0 ? accent : onSurface;

        float unitSize = spDisplay(18f);
        String unit = getContext().getString(R.string.unit);
        float unitWidth = supported ? measure(unit, unitSize, medium, 0f) : 0f;
        float maxValueWidth = Math.max(
                dp(40f),
                (right - left) - dp(52f) - unitWidth - (supported ? dp(8f) : 0f));
        float valueSize = fit(value, maxValueWidth, spDisplay(72f), light, 0f);
        float valueWidth = measure(value, valueSize, light, 0f);
        float totalWidth = valueWidth + (supported ? dp(8f) + unitWidth : 0f);
        float valueLeft = (left + right - totalWidth) / 2f;

        float centerY = heroTop + dp(198f);
        paint.setTypeface(light);
        paint.setTextSize(valueSize);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2f;

        drawText(canvas, value, valueLeft, baseline, valueSize, valueColor,
                light, Paint.Align.LEFT, 0f);
        if (supported) {
            drawText(canvas, unit, valueLeft + valueWidth + dp(8f), baseline,
                    unitSize, onSurfaceVariant, medium, Paint.Align.LEFT, 0f);
        }

        drawStatusPill(canvas, heroBottom, left, right, accent);
    }

    private void drawStatusPill(Canvas canvas, float heroBottom,
                               float left, float right, int accent) {
        String label = statusLabel();
        float textSize = sp(12f);
        float textWidth = measure(label, textSize, medium, 0f);
        float height = dp(30f);
        float width = textWidth + dp(34f);
        float pillLeft = (left + right - width) / 2f;
        float pillTop = heroBottom - dp(18f) - height;

        rect.set(pillLeft, pillTop, pillLeft + width, pillTop + height);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(accent, 0x2E));
        canvas.drawRoundRect(rect, height / 2f, height / 2f, paint);

        paint.setColor(accent);
        canvas.drawCircle(pillLeft + dp(13f), pillTop + height / 2f, dp(3f), paint);

        drawText(canvas, label, pillLeft + dp(23f),
                pillTop + height / 2f - centerOffset(textSize, medium),
                textSize, accent, medium, Paint.Align.LEFT, 0f);
    }

    private void drawCards(Canvas canvas, float top, float left, float right) {
        float gap = dp(12f);
        float cardWidth = ((right - left) - gap) / 2f;
        float cardTop = top + dp(472f);
        float cardBottom = top + dp(584f);

        drawStatCard(canvas, left, cardTop, left + cardWidth, cardBottom,
                R.string.in_label, reading.inUa(), charge);
        drawStatCard(canvas, left + cardWidth + gap, cardTop, right, cardBottom,
                R.string.out_label, reading.outUa(), drain);
    }

    private void drawStatCard(Canvas canvas, float left, float top, float right, float bottom,
                              int labelRes, long microAmps, int accent) {
        rect.set(left, top, right, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(surfaceContainerHigh);
        canvas.drawRoundRect(rect, dp(24f), dp(24f), paint);

        paint.setColor(accent);
        canvas.drawCircle(left + dp(20f), top + dp(25f), dp(3.5f), paint);

        drawText(canvas, label(labelRes), left + dp(32f), top + dp(30f),
                sp(12f), accent, medium, Paint.Align.LEFT, 0.08f);

        boolean supported = reading.supported();
        String value = supported ? milliAmps(microAmps) : DASH;
        float unitSize = spDisplay(12f);
        String unit = getContext().getString(R.string.unit);
        float unitWidth = supported ? measure(unit, unitSize, medium, 0f) : 0f;
        float maxValueWidth = Math.max(
                dp(20f),
                (right - left) - dp(36f) - unitWidth - (supported ? dp(5f) : 0f));
        float valueSize = fit(value, maxValueWidth, spDisplay(32f), medium, 0f);
        float valueWidth = measure(value, valueSize, medium, 0f);
        int valueColor = supported && microAmps > 0 ? accent : onSurface;
        float baseline = bottom - dp(22f);

        drawText(canvas, value, left + dp(18f), baseline, valueSize,
                valueColor, medium, Paint.Align.LEFT, 0f);
        if (supported) {
            drawText(canvas, unit, left + dp(18f) + valueWidth + dp(5f), baseline,
                    unitSize, onSurfaceVariant, medium, Paint.Align.LEFT, 0f);
        }
    }

    private void drawFooter(Canvas canvas, float top, float left, float right) {
        float center = (left + right) / 2f;
        drawText(canvas, getContext().getString(R.string.equation),
                center, top + dp(610f), sp(12f), onSurfaceVariant,
                regular, Paint.Align.CENTER, 0.02f);
        drawText(canvas, getContext().getString(R.string.update_rate),
                center, top + dp(630f), sp(11f), withAlpha(onSurfaceVariant, 0x99),
                regular, Paint.Align.CENTER, 0.02f);
    }

    private int statusAccent() {
        if (!reading.supported()) {
            return idle;
        }
        if (reading.netUa() > 0) {
            return charge;
        }
        if (reading.netUa() < 0) {
            return drain;
        }
        return reading.full() ? charge : idle;
    }

    private int chipAccent() {
        if (!reading.supported()) {
            return idle;
        }
        if (reading.netUa() > 0) {
            return charge;
        }
        if (reading.netUa() < 0) {
            return drain;
        }
        return idle;
    }

    private String statusLabel() {
        if (!reading.supported()) {
            return getContext().getString(R.string.status_no_sensor);
        }
        if (reading.full()) {
            return getContext().getString(R.string.status_full);
        }
        if (reading.netUa() > 0) {
            return getContext().getString(R.string.status_charging);
        }
        if (reading.netUa() < 0) {
            return getContext().getString(R.string.status_discharging);
        }
        return getContext().getString(R.string.status_idle);
    }

    private void describe(Reading reading) {
        Context context = getContext();
        int level = Math.max(reading.level(), 0);
        if (!reading.supported()) {
            setContentDescription(context.getString(R.string.a11y_no_sensor, level));
            return;
        }
        setContentDescription(context.getString(
                R.string.a11y_summary,
                netValue(reading.netUa()),
                milliAmps(reading.inUa()),
                milliAmps(reading.outUa()),
                level,
                statusLabel()));
    }

    private String label(int stringRes) {
        return getContext().getString(stringRes).toUpperCase(Locale.getDefault());
    }

    private static String milliAmps(long microAmps) {
        return Long.toString(Math.round(microAmps / 1000.0));
    }

    private static String netValue(long microAmps) {
        long milliAmps = Math.round(microAmps / 1000.0);
        return milliAmps > 0L ? "+" + milliAmps : Long.toString(milliAmps);
    }

    private void drawText(Canvas canvas, String text, float x, float baseline, float size,
                          int color, Typeface typeface, Paint.Align align, float spacing) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0f);
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        paint.setColor(color);
        paint.setTextAlign(align);
        paint.setLetterSpacing(spacing);
        paint.setFontFeatureSettings("tnum");
        canvas.drawText(text, x, baseline, paint);
    }

    private float measure(String text, float size, Typeface typeface, float spacing) {
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        paint.setLetterSpacing(spacing);
        paint.setFontFeatureSettings("tnum");
        return paint.measureText(text);
    }

    private float fit(String text, float maxWidth, float preferred,
                      Typeface typeface, float spacing) {
        float size = preferred;
        while (size > dp(10f) && measure(text, size, typeface, spacing) > maxWidth) {
            size -= dp(1f);
        }
        return size;
    }

    private float centerOffset(float size, Typeface typeface) {
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        return (metrics.ascent + metrics.descent) / 2f;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    /** Keeps the large readouts usable even when the system font scale is very large. */
    private float spDisplay(float value) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return value * Math.min(metrics.scaledDensity, metrics.density * 1.4f);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
