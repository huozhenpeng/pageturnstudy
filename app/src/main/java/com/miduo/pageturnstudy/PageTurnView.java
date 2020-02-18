package com.miduo.pageturnstudy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PageTurnView extends View {

    //位图列表
    private List<Bitmap> mBitmaps;

    //绘制无数据提示文字画笔
    private Paint mTextPaint;

    private float mTextSizeLarger;
    private float mTextSizeNormal;

    private int mViewWidth;
    private int mViewHeight;


    public PageTurnView(Context context) {
        this(context,null);
    }

    public PageTurnView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public PageTurnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * 设置源数据
     * @param mBitmaps
     */
    public synchronized void setBitmaps(List<Bitmap> mBitmaps)
    {
        if(null==mBitmaps||mBitmaps.size()==0)
        {
            throw  new IllegalArgumentException("no bitmap to display");
        }

        //如果数据长度小于2，则提示用ImageView显示吧
        if(mBitmaps.size()<2)
        {
            throw  new IllegalArgumentException("please use ImageView to display");
        }

        this.mBitmaps=mBitmaps;

        initBitmap();

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth=w;
        mViewHeight=h;
        mClipX=w;

        autoAreaLeft=mViewWidth*1/5f;
        autoAreaRight=mViewWidth*4/5f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null==mBitmaps||mBitmaps.size()==0)
        {
            //绘制默认的提示数据
            defaultDisplay(canvas);
            return;
        }
        drawBitmaps(canvas);
    }

    private float mClipX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()&MotionEvent.ACTION_MASK)
        {

            case MotionEvent.ACTION_UP:
                //判断是否需要自动滑动
                judgeSlideAuto();
                break;


            default:
                mClipX=event.getX();
                invalidate();
                break;
        }
        return true;
    }

    private float autoAreaLeft;
    private float autoAreaRight;
    private void judgeSlideAuto() {

        if(mClipX<autoAreaLeft)
        {
            while (mClipX>0)
            {
                mClipX--;
                invalidate();
            }

        }
        else if(mClipX>autoAreaRight)
        {
            while(mClipX<mViewWidth)
            {
                mClipX++;
                invalidate();
            }
        }
    }

    private void drawBitmaps(Canvas canvas) {
        for(int i=0;i<mBitmaps.size();i++)
        {
            //相当于每张bitmap都是独立的一层
            canvas.save();
            if(i==mBitmaps.size()-1)
            {
                canvas.clipRect(0,0,mClipX,mViewHeight);
            }
            canvas.drawBitmap(mBitmaps.get(i),0,0,null);
            canvas.restore();
        }

    }

    private void defaultDisplay(Canvas canvas) {

        //绘制底色
        canvas.drawColor(Color.WHITE);

        //绘制标题文本
        mTextPaint.setTextSize(mTextSizeLarger);
        mTextPaint.setColor(Color.RED);
        canvas.drawText("FBI  WARNING",mViewWidth/2,mViewHeight/2,mTextPaint);


        //绘制提示文本
        mTextPaint.setTextSize(mTextSizeNormal);
        mTextPaint.setColor(Color.BLACK);
        canvas.drawText("Please set data by use setBitmaps()",mViewWidth/2,mViewHeight/3,mTextPaint);
    }

    /**
     * 设置完数据后，调整bitmap大小以及位置
     */
    private void initBitmap()
    {
        List<Bitmap> temps=new ArrayList<>();
        for (int i=mBitmaps.size()-1;i>=0;i--)
        {
            Bitmap bitmap=Bitmap.createScaledBitmap(mBitmaps.get(i),mViewWidth,mViewHeight,true);
            temps.add(bitmap);
        }
        mBitmaps=temps;

    }


}
