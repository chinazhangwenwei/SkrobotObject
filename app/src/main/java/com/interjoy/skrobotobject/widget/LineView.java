package com.interjoy.skrobotobject.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.interjoy.skrobotobject.MyApplication;

/**
 * Created by ylwang on 2017/8/10.
 */

public class LineView extends View {

    public static float LINE_WIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
            MyApplication.myApplication.getResources().getDisplayMetrics());
    private static int COUNT = 0;
    private static String COLOR[] = {"#009acf", "#2cd03e", "#ffe918", "#ffac53",
            "#ff7377", "#ff4ab0", "#7a60c5"};
    public static int TIME = 2200;

    private final Paint paint = new Paint();

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams();
    }

    private void initParams() {
        COUNT++;
        COUNT = COUNT % 7;
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setColor(Color.parseColor(COLOR[COUNT]));
        angel = 360 / count;
    }

    public void setCount(int count) {
        this.count = count;
        this.angel = 360 / count;
        invalidate();
    }

    private int angel;
    private int count = 10;
    private float startX = 50, endX = 50;
    private float centerX = 100;
    private float centerY = 100;
    private float ratio = 3f / 5f;
    private float size = 500f;

    /**
     * 内圆半径/外圆半径 比例
     *
     * @param ratio
     */
    public void setRatio(float ratio) {
        this.ratio = ratio;
        postInvalidate();
    }

    private float START_X;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        size = centerX * (1 - ratio);//计算线的长度
        startX = centerX - size;//起点
        endX = startX;
        START_X = startX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(centerX, centerY);
        for (int i = 0; i < count; i++) {
            canvas.rotate(angel);
            canvas.drawLine(startX, 0, endX, 0, paint);
        }
//        canvas.drawCircle(0, 0, endX - paint.getStrokeWidth(), paint);
//        canvas.drawCircle(0, 0, startX - paint.getStrokeWidth(), paint);
    }

    private void setStart(float tempX) {
        startX = START_X + tempX;
        invalidate();
    }

    private void setEnd(float tempX) {
        endX = START_X + tempX;
        invalidate();
    }

    public void startAnim() {
        final float startV = 0.0f;//起始透明度
        final float endV = 0.8f;//最终透明度
        ObjectAnimator fadeOutIn = ObjectAnimator.ofFloat(this, "alpha", startV, endV);
//        fadeOutIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOutIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                float d = (f - startV) / endV;
                if (d - 0.5f < 0.001) {//改变end
                    setEnd(size * (d * 2.0f));
                } else {//大于 改变start
                    setStart(size * ((d - 0.5f) * 2.0f));
                }
            }
        });
        fadeOutIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animFinishListener.onFinish(LineView.this);
            }
        });
        fadeOutIn.setDuration(TIME);
        fadeOutIn.start();
    }

    public void reset() {
        startX = START_X;//起点
        endX = START_X;
    }


    public interface onAnimFinishListener {
        void onFinish(LineView lineView);
    }

    private onAnimFinishListener animFinishListener;

    public void setOnAnimFinishListener(onAnimFinishListener animFinishListener) {
        this.animFinishListener = animFinishListener;
    }
}