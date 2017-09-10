package com.interjoy.skrobotrecongnize.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.interjoy.skrobotrecongnize.R;

/**
 * Title:
 * Description:
 * Company: 北京盛开互动科技有限公司
 * Tel: 010-62538800
 * Mail: support@interjoy.com.cn
 *
 * @author zhangwenwei
 * @date 2017/9/7
 */
public class BolderTextView extends AppCompatTextView {
    private TextPaint strokePaint;
    private static float STROKE_WIDTH = 6;

    public BolderTextView(Context context) {
        super(context);
    }

    public BolderTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTextScaleX(1.1f);
    }

    public BolderTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (strokePaint == null) {
            strokePaint = new TextPaint();
        }
        // 复制原来TextViewg画笔中的一些参数
        TextPaint paint = getPaint();
        strokePaint.setTextSize(paint.getTextSize());
        strokePaint.setTypeface(paint.getTypeface());
        strokePaint.setFlags(paint.getFlags());
        strokePaint.setAlpha(paint.getAlpha());
        strokePaint.setTextScaleX(1.1f);

        // 自定义描边效果
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setColor(getResources().getColor(R.color.text_bolder));
        strokePaint.setStrokeWidth(STROKE_WIDTH);

        String text = getText().toString();
        //在文本底层画出带描边的文本
        canvas.drawText(text, (getWidth() - strokePaint.measureText(text)) / 2,
                getBaseline(), strokePaint);


        super.onDraw(canvas);
    }
}
