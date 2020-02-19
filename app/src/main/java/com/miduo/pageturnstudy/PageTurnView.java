package com.miduo.pageturnstudy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
        this.context=context;
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

        harfWidth=mViewWidth*1/2f;
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
    private float mCurPointX;
    private int pageIndex;
    private boolean moveValidate;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()&MotionEvent.ACTION_MASK)
        {

            case MotionEvent.ACTION_DOWN:
                //上一页、下一页的判断还是应该在down中进行

                //因为我们每次只绘制两张图片
                //所以在翻上一页的时候必须先减
                //在翻下一页的时候，在结束后再加
                mCurPointX=event.getX();
                if(mCurPointX<autoAreaLeft)
                {
                    if(pageIndex==0)
                    {
                        moveValidate=false;
                        Toast.makeText(context,"第一页了",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //代表上一页
                        isNextPage=false;
                        pageIndex--;
                        moveValidate=true;
                        mClipX=mCurPointX;
                    }

                }
                else if(mCurPointX>autoAreaRight)
                {
                    if(pageIndex==mBitmaps.size()-1)
                    {
                        moveValidate=false;
                        Toast.makeText(context,"最后一页了",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //代表要翻下一页
                        isNextPage=true;
                        //pageIndex++;
                        moveValidate=true;
                        mClipX=mCurPointX;
                    }

                }
                else
                {
                    //其他区域无效，不发生翻页效果
                    moveValidate=false;
                }
                break;
            case MotionEvent.ACTION_UP:
                //判断是否需要自动滑动
                if(moveValidate)
                {
                    judgeSlideAuto();

                    //动画结束之后再判断

                    if(isNextPage&&mClipX<=0)
                    {
                        pageIndex++;
                        mClipX=mViewWidth;
                        invalidate();
                    }


                }
                break;


            default:
                if(moveValidate)
                {
                    mClipX=event.getX();
                    invalidate();
                }
                break;
        }
        return true;
    }

    private float autoAreaLeft;
    private float autoAreaRight;
    private float harfWidth;
    private boolean isNextPage;
    private void judgeSlideAuto() {

        if(mClipX<=harfWidth)
        {
            while (mClipX>0)
            {
                mClipX--;
                invalidate();
            }
        }
        else if(mClipX>harfWidth)
        {
            while(mClipX<mViewWidth)
            {
                mClipX++;
                invalidate();
            }
        }
    }

    private Context context;
    private void drawBitmaps(Canvas canvas) {


        pageIndex=pageIndex<0?0:pageIndex;
        pageIndex=pageIndex>mBitmaps.size()?mBitmaps.size():pageIndex;
        //相当于每张bitmap都是独立的一层
        //最后绘制的会在最上层

        //其实每次只绘制两张就可以了，最初的时候就是最上面的两张
        int start=mBitmaps.size()-pageIndex-2;
        int end=mBitmaps.size()-pageIndex;

        if(start<0)
        {
            Toast.makeText(context,"last",Toast.LENGTH_SHORT).show();

            //强制重置起始位置
            start=0;
            end=1;
        }
        for(int i=start;i<end;i++)
        {
            canvas.save();
            if(i==end-1)
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
