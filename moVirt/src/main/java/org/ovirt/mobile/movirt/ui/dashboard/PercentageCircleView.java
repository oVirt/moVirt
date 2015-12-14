package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import org.ovirt.mobile.movirt.R;

import java.text.DecimalFormat;

public class PercentageCircleView extends View {

    private static final float FOREGROUND_COLOR_A_MAX_PERCENTAGE = 0.5f;
    private static final float FOREGROUND_COLOR_B_MAX_PERCENTAGE = 0.75f;
    private static final float FOREGROUND_COLOR_C_MAX_PERCENTAGE = 1.0f;

    private static final float DEFAULT_MAX_PERCENTAGE_VALUE = 100f;
    private static final String DEFAULT_DECIMAL_FORMAT_PATTERN = "#";
    private static final int MAX_ANGLE = 360;

    //attr
    private int wholeBackgroundColor = Color.parseColor("#ffffff");
    private int backgroundColor = Color.parseColor("#666666");
    private int foregroundColorA = Color.parseColor("#99cc03");//when percentage < FOREGROUND_COLOR_A_MAX_PERCENTAGE
    private int foregroundColorB = Color.parseColor("#ec7108");//when percentage >= FOREGROUND_COLOR_A_MAX_PERCENTAGE && < FOREGROUND_COLOR_B_MAX_PERCENTAGE
    private int foregroundColorC = Color.parseColor("#ce0000");//when percentage >= FOREGROUND_COLOR_C_MAX_PERCENTAGE
    private int textColor;
    private int strokeWidth = 10;// default stroke width
    private int startAngle = -90;// default 12 o'clock
    private boolean needShowText = true;// default show text

    private Context context;
    private RectF wholeRectF, foregroundRectF, backgroundRectF;
    private Paint wholeBackgroundPaint;
    private Paint backgroundPaint;
    private Paint foregroundPaint;
    private Paint textPaint;
    private int currentForegroundColor = foregroundColorA;
    private int angleStep = 0;
    private int minWidth;
    private float maxPercentageValue = DEFAULT_MAX_PERCENTAGE_VALUE;
    private float percentageValue = 0;
    private DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT_PATTERN);
    private String summary = "";

    public PercentageCircleView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PercentageCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        parseAttributes(attrs);
        init();
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.percentage_circle_view);
        wholeBackgroundColor = a.getColor(R.styleable.percentage_circle_view_whole_background_color, wholeBackgroundColor);
        foregroundColorA = a.getColor(R.styleable.percentage_circle_view_foreground_color_a, foregroundColorA);
        foregroundColorB = a.getColor(R.styleable.percentage_circle_view_foreground_color_b, foregroundColorB);
        foregroundColorC = a.getColor(R.styleable.percentage_circle_view_foreground_color_c, foregroundColorC);
        backgroundColor = a.getColor(R.styleable.percentage_circle_view_background_color, backgroundColor);
        textColor = a.getColor(R.styleable.percentage_circle_view_text_color, textColor);
        strokeWidth = a.getInt(R.styleable.percentage_circle_view_stroke_width, strokeWidth);
        startAngle = a.getInt(R.styleable.percentage_circle_view_start_angle, startAngle);
        needShowText = a.getBoolean(R.styleable.percentage_circle_view_need_show_text, needShowText);

        textColor = textColor == 0 ? currentForegroundColor : textColor;
        a.recycle();
    }

    private void init() {
        initWholeBackgroundPaint();
        initBackgroundPaint();
        initForegroundPaint();
        initTextPaint();
        initRectF();
    }

    private void initWholeBackgroundPaint() {
        wholeBackgroundPaint = new Paint();
        wholeBackgroundPaint.setAntiAlias(true);
        wholeBackgroundPaint.setColor(wholeBackgroundColor);
    }

    private void initBackgroundPaint() {
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(backgroundColor);
    }

    private void initForegroundPaint() {
        foregroundPaint = new Paint();
        foregroundPaint.setAntiAlias(true);
        foregroundPaint.setDither(true);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeCap(Paint.Cap.ROUND);
        foregroundPaint.setStrokeWidth(strokeWidth);
        foregroundPaint.setColor(currentForegroundColor);
    }

    private void initTextPaint() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
    }

    private void initRectF() {
        backgroundRectF = new RectF();
        foregroundRectF = new RectF();
        wholeRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heigthWithoutPadding) {
            size = heigthWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            minWidth = Math.min(getWidth(), getHeight());
            int rLeft = strokeWidth / 2;
            int rTop = strokeWidth / 2;
            int rRight = minWidth - strokeWidth / 2;
            int rBottom = minWidth - strokeWidth / 2;
            backgroundRectF.set(rLeft, rTop, rRight, rBottom);
            foregroundRectF.set(rLeft, rTop, rRight, rBottom);
            wholeRectF.set(strokeWidth / 2, strokeWidth / 2, minWidth - strokeWidth / 2, minWidth - strokeWidth / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWholeBackground(canvas);
        drawBackground(canvas);
        drawForeground(canvas);
        drawTextAndSummary(canvas);
    }

    private void drawWholeBackground(Canvas canvas) {
        canvas.drawCircle(foregroundRectF.centerX(), foregroundRectF.centerY(), foregroundRectF.height() / 2, wholeBackgroundPaint);
    }

    private void drawTextAndSummary(Canvas canvas) {
        if (!needShowText) return;
        //draw progress text
        String text = decimalFormat.format(percentageValue);
        int textSize = (int) ((minWidth - strokeWidth * 2) / 3.5);
        textPaint.setTextSize(textSize);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = (float) Math.ceil(fm.descent - fm.top);
        float textWidth = textPaint.measureText(text);
        float x = (minWidth - textWidth) / 2;
        float y = (minWidth - textHeight) / 2 + textSize;
        canvas.drawText(text, x, y, textPaint);

        //draw summary
        String summary = String.valueOf(this.summary);
        int summaryTextSize = (int) ((minWidth - strokeWidth * 2) / 11.0);
        textPaint.setTextSize(summaryTextSize);
        float summaryTextWidth = textPaint.measureText(summary);
        float summaryX = (minWidth - summaryTextWidth) / 2;
        float summaryY = y + summaryTextSize * 3 / 2;
        canvas.drawText(summary, summaryX, summaryY, textPaint);
    }

    private void drawForeground(Canvas canvas) {
        int startAngle = angleStep + this.startAngle;
        int sweepAngle = (int) ((percentageValue / maxPercentageValue) * MAX_ANGLE);
        if (Float.compare(percentageValue / maxPercentageValue, FOREGROUND_COLOR_A_MAX_PERCENTAGE) < 0) {
            currentForegroundColor = foregroundColorA;
        } else if (Float.compare(percentageValue / maxPercentageValue, FOREGROUND_COLOR_B_MAX_PERCENTAGE) < 0) {
            currentForegroundColor = foregroundColorB;
        } else {
            currentForegroundColor = foregroundColorC;
        }
        foregroundPaint.setColor(currentForegroundColor);
        canvas.drawArc(foregroundRectF, startAngle, sweepAngle, false, foregroundPaint);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawArc(backgroundRectF, startAngle, MAX_ANGLE, false, backgroundPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * invalidate view
     */
    public void invalidateUi() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     *
     * @param maxPercentageValue >0
     */
    public void setMaxPercentageValue(float maxPercentageValue) {
        if (Float.compare(maxPercentageValue, 0) > 0) {
            this.maxPercentageValue = maxPercentageValue;
        } else {
            this.maxPercentageValue = DEFAULT_MAX_PERCENTAGE_VALUE;
        }
    }

    public float getMaxPercentageValue() {
        return maxPercentageValue;
    }

    /**
     *
     * @param percentageValue >=0
     */
    public void setPercentageValue(float percentageValue) {
        if (Float.compare(percentageValue, 0) >= 0) {
            this.decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT_PATTERN);
            this.percentageValue = Float.compare(percentageValue, maxPercentageValue) > 0 ? maxPercentageValue : percentageValue;
            invalidateUi();
        }
    }

    /**
     *
     * @param percentageValue >=0
     * @param decimalFormat
     */
    public void setPercentageValue(float percentageValue, DecimalFormat decimalFormat) {
        if (Float.compare(percentageValue, 0) >= 0) {
            this.decimalFormat = decimalFormat != null ? decimalFormat : new DecimalFormat(DEFAULT_DECIMAL_FORMAT_PATTERN);
            this.percentageValue = Float.compare(percentageValue, maxPercentageValue) > 0 ? maxPercentageValue : percentageValue;
            invalidateUi();
        }
    }

    public float getPercentageValue(){
        return percentageValue;
    }

    public void setSummary(String summary) {
        if (summary != null) {
            this.summary = summary;
        }
    }

    public String getSummary() {
        return summary;
    }
}
