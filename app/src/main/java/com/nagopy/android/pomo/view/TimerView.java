package com.nagopy.android.pomo.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.nagopy.android.pomo.R;

import java.util.Locale;

import timber.log.Timber;

public class TimerView extends View {

    private RectF rect;
    private Paint paint;
    private TextPaint textPaint;
    private float totalSeconds = 0;
    private float currentSecond = 0;
    private int deviceWidth;
    private int deviceHeight;

    @ColorInt
    private int colorTotal;
    @ColorInt
    private int colorMin;
    @ColorInt
    private int colorSec;

    public TimerView(Context context) {
        super(context);
        init(null, 0);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.primary_text, null));

        rect = new RectF();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(deviceWidth, deviceHeight);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(deviceWidth, deviceHeight);
        float x = size / 2; // 中心X
        float y = size / 2; // 中心Y
        float radius = size * 2 / 5; // 半径
        int strokeWidth = (int) radius / 9; // 太さ
        paint.setStrokeWidth(strokeWidth);
        rect.set(x - radius, y - radius, x + radius, y + radius);

        // TOTAL
        paint.setColor(colorTotal);
        canvas.drawArc(rect, -90, 360, false, paint);

        // TOTAL (progress)
        radius -= strokeWidth * 1.5;
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(colorMin);
        rect.set(x - radius, y - radius, x + radius, y + radius);

        float percent = totalSeconds > 0 ? currentSecond / totalSeconds : 0;
        canvas.drawArc(rect, -90, percent * 360, false, paint);

        // SEC (progress)
        radius -= strokeWidth * 1.5;
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(colorSec);
        rect.set(x - radius, y - radius, x + radius, y + radius);
        int min = (int) (currentSecond / 60);
        float sec = currentSecond % 60;
        float edge = sec / 60 * 360;
        if (min % 2 == 0) {
            canvas.drawArc(rect, -90, edge, false, paint);
        } else {
            canvas.drawArc(rect, -90 + edge, 360 - edge, false, paint);
        }

        textPaint.setTextSize(strokeWidth * 3);

        String formatMMSS = formatMMSS((int) (totalSeconds - currentSecond));
        float textHeight = measureTextHeight(formatMMSS);
        canvas.drawText(formatMMSS, x, y + textHeight / 2, textPaint);

        Timber.v("onDraw totalSeconds=%f, currentSecond=%f, percent=%f", totalSeconds, currentSecond, percent);
    }

    public void setSeconds(int totalSeconds, int currentSecond) {
        this.totalSeconds = totalSeconds;
        this.currentSecond = currentSecond;
        invalidate();
    }

    public void updateCurrentSecond(int currentSecond) {
        this.currentSecond = currentSecond;
        invalidate();
    }

    private String formatMMSS(int sec) {
        return String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60);
    }

    private float measureTextHeight(@NonNull String text) {
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }

    public void setColors(@ColorRes int colorTotal, @ColorRes int colorMin, @ColorRes int colorSec) {
        Resources res = getResources();
        this.colorTotal = ResourcesCompat.getColor(res, colorTotal, null);
        this.colorMin = ResourcesCompat.getColor(res, colorMin, null);
        this.colorSec = ResourcesCompat.getColor(res, colorSec, null);
    }
}
