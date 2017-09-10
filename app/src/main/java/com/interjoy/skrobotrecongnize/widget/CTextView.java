package com.interjoy.skrobotrecongnize.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import com.interjoy.skrobotrecongnize.MyApplication;
import com.interjoy.skrobotrecongnize.R;

/**
 * Created by ylwang on 2017/8/11.
 */

public class CTextView extends android.support.v7.widget.AppCompatTextView {
    private Paint paint;
    public static int TIME = 2200;
    public static float LINE_WIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
            MyApplication.myApplication.getResources().getDisplayMetrics());

    public CTextView(Context context) {
        this(context, null);
    }

    public CTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams();
    }

    private int viewWidth, viewHeight;//控件宽高

    private void initParams() {
//        viewWidth = viewHeight = 200;
        paint = new Paint();
//        paint.setColor(Color.parseColor("#FFB300"));
        paint.setStrokeWidth(LINE_WIDTH);
        TextPaint tp = getPaint();
        tp.setFakeBoldText(true);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        setShadowLayer(LINE_WIDTH, LINE_WIDTH, LINE_WIDTH, getResources().getColor(R.color.black_trans));
        setGravity(Gravity.CENTER);
    }

    public void setAge(int age) {
        if (age <= 16) {
            paint.setColor(Color.rgb(0, 255, 256 - age * 16));//00ffff
        } else if (16 < age && age <= 32) {
            paint.setColor(Color.rgb((age - 16) * 16 - 1, 255, 0));//00ff00
        } else if (32 < age && age <= 48) {
            paint.setColor(Color.rgb(255, 256 - ((age - 32) * 16), 0));//ffff00
        } else if (48 < age && age <= 64) {
            paint.setColor(Color.rgb(255, 0, (age - 48) * 16 - 1));//ff0000
        } else if (64 < age && age < 80) {
            paint.setColor(Color.rgb(256 - ((age - 64) * 16), 0, 255));//ff00ff
        } else {
            paint.setColor(Color.rgb(0, 0, 255));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, viewWidth / 2f - paint.getStrokeWidth(), paint);
    }

    public void startAnim() {
        ObjectAnimator sx = ObjectAnimator.ofFloat(this, "scaleY", 0.75f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(this, "scaleX", 0.75f, 1f);
        ObjectAnimator fadeOutIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 0.7f, 0.7f, 0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(sx).with(sy).with(fadeOutIn);
        animSet.setDuration(TIME);
        animSet.start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (animFinishListener != null) {
                    animFinishListener.onFinish(CTextView.this);
                }
            }
        });
    }

    public interface onAnimFinishListener {
        void onFinish(CTextView cTextView);
    }

    private onAnimFinishListener animFinishListener;

    public void setOnAnimFinishListener(onAnimFinishListener animFinishListener) {
        this.animFinishListener = animFinishListener;
    }
}
