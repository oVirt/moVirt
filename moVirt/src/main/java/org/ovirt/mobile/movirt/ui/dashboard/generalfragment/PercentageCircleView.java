package org.ovirt.mobile.movirt.ui.dashboard.generalfragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.util.usage.MemorySize;
import org.ovirt.mobile.movirt.util.usage.Percentage;
import org.ovirt.mobile.movirt.util.usage.UsageResource;

public class PercentageCircleView extends View {

    private static final double FOREGROUND_COLOR_A_MAX_PERCENTAGE = 0.5f;
    private static final double FOREGROUND_COLOR_B_MAX_PERCENTAGE = 0.75f;
    private static final double FOREGROUND_COLOR_C_MAX_PERCENTAGE = 1.0f;

    private static final int MAX_ANGLE = 360;

    //attr
    private int wholeBackgroundColor = Color.parseColor("#ffffff");
    private int backgroundColor = Color.parseColor("#666666");
    private int foregroundColorA = Color.parseColor("#3f9c35");//when percentage < FOREGROUND_COLOR_A_MAX_PERCENTAGE
    private int foregroundColorB = Color.parseColor("#ec7a08");//when percentage >= FOREGROUND_COLOR_A_MAX_PERCENTAGE && < FOREGROUND_COLOR_B_MAX_PERCENTAGE
    private int foregroundColorC = Color.parseColor("#cc0000");//when percentage >= FOREGROUND_COLOR_C_MAX_PERCENTAGE
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
    private UsageResource maxResource = new MemorySize();
    private UsageResource usedResource = new MemorySize();
    private String usedResourceDescription = "";

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

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInCircle(x, y)) {
                    setActivated(true);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isInCircle(x, y)) {
                    setActivated(false);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                setActivated(false);
                invalidate();
                break;
        }

        return true;
    }

    private boolean isInCircle(float x, float y) {
        float dx = foregroundRectF.centerX() - x;
        float dy = foregroundRectF.centerY() - y;

        return Math.pow(dx, 2) + Math.pow(dy, 2) <= Math.pow(foregroundRectF.height() / 2, 2);
    }

    private void drawTextAndSummary(Canvas canvas) {
        if (!needShowText) return;
        //draw progress text
        String text = "";
        if (usedResource instanceof MemorySize) {
            text = ((MemorySize) usedResource).getReadableValueAsString(1, ((MemorySize) maxResource).getReadableUnit());
        } else if (usedResource instanceof Percentage) {
            text = usedResource.toString();
        }

        int textSize = (int) ((minWidth - strokeWidth * 2) / 3.5);
        textPaint.setTextSize(textSize);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = (float) Math.ceil(fm.descent - fm.top);
        float textWidth = textPaint.measureText(text);
        float x = (minWidth - textWidth) / 2;
        float y = (minWidth - textHeight) / 2 + textSize;
        canvas.drawText(text, x, y, textPaint);

        //draw summary
        int summaryTextSize = (int) ((minWidth - strokeWidth * 2) / 11.0);
        textPaint.setTextSize(summaryTextSize);
        float summaryTextWidth = textPaint.measureText(usedResourceDescription);
        float summaryX = (minWidth - summaryTextWidth) / 2;
        float summaryY = y + summaryTextSize * 3 / 2;
        canvas.drawText(usedResourceDescription, summaryX, summaryY, textPaint);
    }

    private void drawForeground(Canvas canvas) {
        double resourcePercentageRatio = usedResource.getValue() / (double) (maxResource.getValue() == 0 ? 1 : maxResource.getValue());
        int startAngle = angleStep + this.startAngle;
        int sweepAngle = (int) (resourcePercentageRatio * MAX_ANGLE);
        if (Double.compare(resourcePercentageRatio, FOREGROUND_COLOR_A_MAX_PERCENTAGE) < 0) {
            currentForegroundColor = foregroundColorA;
        } else if (Double.compare(resourcePercentageRatio, FOREGROUND_COLOR_B_MAX_PERCENTAGE) < 0) {
            currentForegroundColor = foregroundColorB;
        } else {
            currentForegroundColor = foregroundColorC;
        }
        foregroundPaint.setColor(adjustColor(currentForegroundColor));
        canvas.drawArc(foregroundRectF, startAngle, sweepAngle, false, foregroundPaint);
    }

    private void drawBackground(Canvas canvas) {
        backgroundPaint.setColor(adjustColor(backgroundColor));
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

    public void setMaxResource(UsageResource maxResource) {
        if (maxResource != null) {
            this.maxResource = maxResource;
        }
    }

    public UsageResource getMaxResource() {
        return maxResource;
    }

    public void setUsedResource(UsageResource usedResource) {
        if (maxResource != null) {
            this.usedResource = usedResource;
            invalidateUi();
        }
    }

    public UsageResource getUsedResource() {
        return usedResource;
    }

    public void setUsedResourceDescription(String usedResourceDescription) {
        if (usedResourceDescription != null) {
            this.usedResourceDescription = usedResourceDescription;
        }
    }

    public String getUsedResourceDescription() {
        return usedResourceDescription;
    }

    /**
     * Used for altering color in touch up/down events
     *
     * @param color color
     * @return adjusted color
     */
    private int adjustColor(int color) {
        if (isActivated()) {
            final double valueShift = 0.07;
            float[] hsbVals = new float[3];

            Color.colorToHSV(color, hsbVals);
            hsbVals[2] += valueShift;
            color = Color.HSVToColor(hsbVals);
        }
        return color;
    }
}
