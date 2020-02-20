package com.miduo.pageturnstudy.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FoldView  extends View {
    public FoldView(Context context) {
        this(context,null);
    }

    public FoldView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public FoldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mPath=new Path();
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);

        mSldeHandler=new SlideHandler();
    }

    private int mViewWidth;
    private int mViewHeight;
    //防止精度损失，额外的附加值
    private float mValueAdded;
    private float mBuffArea;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewHeight=h;
        mViewWidth=w;
        computeShortSizeRegion();
        mRegionCurrent.set(0,0,mViewWidth,mViewHeight);
    }

    private Path mPath;
    private float pointX;
    private float pointY;
    private Paint mPaint;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.reset();

        //绘制底色
        canvas.drawColor(Color.WHITE);

        /**
         * 如果坐标点在右下角，则不执行
         */
        if(pointX==0&&pointY==0)
        {
            return;
        }

        float mK=mViewWidth-pointX;
        float mL=mViewHeight-pointY;
        //需要重复使用的参数，计算好，避免重复计算
        float temp= (float) (Math.pow(mL,2)+Math.pow(mK,2));

        float sizeShort=temp/(2f*mK);
        float sizeLong=temp/(2f*mL);




        /*
         * 根据长短边边长计算旋转角度并确定mRatio的值
         */
        if (sizeShort < sizeLong) {
            mRatio = Ratio.SHORT;
            float sin = (mK - sizeShort) / sizeShort;
            mDegrees = (float) (Math.asin(sin) / Math.PI * 180);
        } else {
            mRatio = Ratio.LONG;
            float cos = mK / sizeLong;
            mDegrees = (float) (Math.acos(cos) / Math.PI * 180);
        }





        mPath.moveTo(pointX,pointY);

        // 移动路径起点至触摸点
        mPath.moveTo(pointX, pointY);

        //
        mPathFoldAndNext.moveTo(pointX,pointY);

        if (sizeLong > mViewHeight) {
            // 计算……额……按图来AN边~
            float an = sizeLong - mViewHeight;

            // 三角形AMN的MN边
            float largerTrianShortSize = an / (sizeLong - (mViewHeight - pointY)) * (mViewWidth - pointX);

            // 三角形AQN的QN边
            float smallTrianShortSize = an / sizeLong * sizeShort;

            /*
             * 计算参数
             */
            float topX1 = mViewWidth - largerTrianShortSize;
            float topX2 = mViewWidth - smallTrianShortSize;
            float btmX2 = mViewWidth - sizeShort;


            /*
             * 生成四边形路径
             */
            mPath.lineTo(topX1, 0);
            mPath.lineTo(topX2, 0);
            mPath.lineTo(btmX2, mViewHeight);
            mPath.close();

            /*
             * 生成包含折叠和下一页的路径
             */
            mPathFoldAndNext.lineTo(topX1, 0);
            mPathFoldAndNext.lineTo(mViewWidth, 0);
            mPathFoldAndNext.lineTo(mViewWidth, mViewHeight);
            mPathFoldAndNext.lineTo(btmX2, mViewHeight);
            mPathFoldAndNext.close();

        } else {

            float leftY=mViewHeight-sizeLong;
            float btmX=mViewWidth-sizeShort;
            /*
             * 生成三角形路径
             */
            mPath.lineTo(mViewWidth, leftY);
            mPath.lineTo(btmX, mViewHeight);
            mPath.close();

            //折叠区域和下一页区域计算
            mPathFoldAndNext.lineTo(mViewWidth, leftY);
            mPathFoldAndNext.lineTo(mViewWidth, mViewHeight);
            mPathFoldAndNext.lineTo(btmX, mViewHeight);
            mPathFoldAndNext.close();

        }

        // 绘制路径
        canvas.drawPath(mPath, mPaint);
        drawBitmaps(canvas);

    }
    private boolean isLastPage;
    private int mPageIndex;
    private List<Bitmap> mBitmaps;
    private Context context;


    private float mDegrees;// 当前Y边长与Y轴的夹角


    private void drawBitmaps(Canvas canvas) {
        // 绘制位图前重置isLastPage为false
        isLastPage = false;

        // 限制pageIndex的值范围
        mPageIndex = mPageIndex < 0 ? 0 : mPageIndex;
        mPageIndex = mPageIndex > mBitmaps.size() ? mBitmaps.size() : mPageIndex;

        // 计算数据起始位置
        int start = mBitmaps.size() - 2 - mPageIndex;
        int end = mBitmaps.size() - mPageIndex;

        /*
         * 如果数据起点位置小于0则表示当前已经到了最后一张图片
         */
        if (start < 0) {
            // 此时设置isLastPage为true
            isLastPage = true;

            // 并显示提示信息
            Toast.makeText(context,"last",Toast.LENGTH_SHORT).show();

            // 强制重置起始位置
            start = 0;
            end = 1;
        }

        /*
         * 定义区域
         */
        Region regionFold = null;
        Region regionNext = null;

        /*
         * 通过路径成成区域
         */
        regionFold = computeRegion(mPath);

        //其实这个区域包含了折叠区域
        regionNext = computeRegion(mPathFoldAndNext);

        /*
         * 计算当前页的区域
         */
        canvas.save();
//        canvas.clipRegion(mRegionCurrent);
//        canvas.clipRegion(regionNext, Region.Op.DIFFERENCE);
        canvas.drawBitmap(mBitmaps.get(end - 1), 0, 0, null);
        canvas.restore();

        /*
         * 计算折叠页的区域
         */
        canvas.save();
//        canvas.clipRegion(regionFold);

        canvas.translate(pointX, pointY);

        /*
         * 根据长短边标识计算折叠区域图像
         */
        if (mRatio == Ratio.SHORT) {
            canvas.rotate(90 - mDegrees);
            canvas.translate(0, -mViewHeight);
            canvas.scale(-1, 1);
            canvas.translate(-mViewWidth, 0);
        } else {
            canvas.rotate(-(90 - mDegrees));
            canvas.translate(-mViewWidth, 0);
            canvas.scale(1, -1);
            canvas.translate(0, -mViewHeight);
        }

        canvas.drawBitmap(mBitmaps.get(end - 1), 0, 0, null);
        canvas.restore();

        /*
         * 计算下一页的区域
         */
        canvas.save();
//        canvas.clipRegion(regionNext);
//        canvas.clipRegion(regionFold, Region.Op.DIFFERENCE);
        canvas.drawBitmap(mBitmaps.get(start), 0, 0, null);

    }

    private float mAutoAreaRight;
    private float mAutoAreaBottom;
    private float mAutoAreaLeft;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction())
        {
            case MotionEvent.ACTION_UP:
                float x=event.getX();
                float y=event.getY();
                //如果当前触摸点位于右下自滑动区域
                if(x>mAutoAreaRight&&y>mAutoAreaBottom)
                {
                    mSlide=Slide.RIGHT_BOTTOM;

                    justSlide(x,y);
                }

                if(x<mAutoAreaLeft)
                {
                    mSlide=Slide.LEFT_BOTTOM;
                    justSlide(x,y);

                }
                break;
                default:
                    //判断触摸点如果不在短边形成的圆的有效区域内，则通过x坐标强行重算y坐标
                    if(!mRegionShortSize.contains((int)pointX,(int)pointY))
                    {
                        pointY= (float) (mViewHeight-(Math.sqrt(Math.pow(mViewWidth,2)-Math.pow(pointX,2))));

                        //精度附加值，避免精度损失
                        pointY=pointY+mValueAdded;
                    }

                    //防止过分往下折
                    float area=mViewHeight-mBuffArea;
                    if(!isSlide&&pointY>=area)
                    {
                        pointY=area;
                    }

                    pointX=event.getX();
                    pointY=event.getY();
                    invalidate();
                    break;
        }


        return true;
    }

    private void justSlide(float x, float y) {
        mStart_BR_X=x;
        mStart_BR_Y=y;

        isSlide=true;
        slide();
    }

    private Region mRegionShortSize;
    /**
     * 计算触摸点有效区域
     */
    private void computeShortSizeRegion()
    {
        //短边圆形路径
        Path shortSizePath=new Path();

        //用来装载path边界值的RectF对象
        RectF shortSizeRectF=new RectF();

        //添加圆形到path
        shortSizePath.addCircle(0,mViewHeight,mViewWidth, Path.Direction.CCW);

        shortSizePath.computeBounds(shortSizeRectF,true);

        //将path转化为region
        //region中有方法可以快速判断一个点是否在指定的区域内
        //该方法的作用是将path区域和clip区域取交集
        mRegionShortSize.setPath(shortSizePath,new Region((int)shortSizeRectF.left,(int)shortSizeRectF.top,(int)shortSizeRectF.right,(int)shortSizeRectF.bottom));

    }

    private class SlideHandler extends Handler
    {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //循环调用滑动计算
            FoldView.this.slide();

            FoldView.this.invalidate();
        }

        public void sleep(int delayMillis)
        {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0),delayMillis);
        }
    }

    private boolean isSlide;
    private float mStart_BR_Y;
    private float mStart_BR_X;
    private SlideHandler mSldeHandler;

    private void slide() {

        if(!isSlide)
        {
            return;
        }

        if(mSlide==Slide.RIGHT_BOTTOM&&pointX<mViewWidth)
        {
            pointX+=10;
            //直线方程
            //根据x坐标重新计算y坐标的值
            pointY= mStart_BR_Y + ((pointX - mStart_BR_X) * (mViewHeight - mStart_BR_Y)) / (mViewWidth - mStart_BR_X);


            mSldeHandler.sleep(25);
        }


        if(mSlide==Slide.LEFT_BOTTOM&&pointX>-mViewWidth)
        {

            // 则让x坐标自减
            pointX -= 20;

            // 并根据x坐标的值重新计算y坐标的值
            pointY = mStart_BR_Y + ((pointX - mStart_BR_X) * (mViewHeight - mStart_BR_Y)) / (-mViewWidth - mStart_BR_X);

            // 让SlideHandler处理重绘
            mSldeHandler.sleep(25);


        }
    }


    /**
     * 为isSlide提供对外的停止方法便于必要时释放滑动动画
     * 比如说Activity的onDestroy方法中
     */
    public void slideStop()
    {
        isSlide=false;
    }


    private Slide mSlide;
    /**
     * 枚举类 定义手指抬起时自滑动的方向
     */
    private enum  Slide {
        LEFT_BOTTOM, RIGHT_BOTTOM
    }

    //当前页区域，其实就是控件的大小
    private Region mRegionCurrent;

    //一个包含折叠和下一页区域的Path对象
    private Path mPathFoldAndNext;

    /**
     * 将折叠和下一页区域的path对象转化为region对象
     * @param path
     * @return
     */
    public Region computeRegion(Path path)
    {
        Region region=new Region();
        RectF f=new RectF();
        path.computeBounds(f,true);
        region.setPath(path, new Region((int) f.left, (int) f.top, (int) f.right, (int) f.bottom));
        return region;
    }

    private Ratio mRatio;// 定义当前折叠边长

    /**
     * 枚举类定义长边短边
     */
    private enum Ratio {
        LONG, SHORT
    }



}
