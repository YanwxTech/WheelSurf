package yvan.com.surfaceviewtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Created by Yvan on 2015/7/8.
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Paint mPaint;


    private int mPadding;//最小内边距
    private int mCenter;//中心点距离
    private int mRadius;//外圆半径

    private int color_one = Color.parseColor("#BF002D");
    private int color_two = Color.parseColor("#FFE366");
    private int[] colors =
            new int[]{color_one, color_two, color_one, color_two, color_one, color_two};
    private String[] mText =
            new String[]{"小米Note", "单反相机", "谢谢参与", "Ipad", "万元大奖", "谢谢参与"};
    private int[] mPic = new int[]{R.drawable.note, R.drawable.df, R.drawable.thank, R.drawable.ipad, R.drawable.gift, R.drawable.thank};
    private int mCount = mText.length;
    private int sweepAngle = 360 / mCount;


    private float inPadding =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
    private RectF mRange;
    private Paint textPaint;

    private boolean isRunning = false;
    private boolean isShouldEnd = false;
    private volatile float mStartAngle = 0;
    private float mSpeed = 0;
    private int mWinning;

    public LuckyPan(Context context) {
        this(context, null);
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        this(context, attrs, 0);


    }

    public LuckyPan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(width, width);
        mPadding = Math.min(Math.min(getPaddingLeft(), getPaddingRight()), Math.min(getPaddingTop(), getPaddingBottom()));
        mCenter = width / 2;
        mRadius = width / 2 - mPadding;
        mRange = new RectF(
                mPadding + inPadding, mPadding + inPadding,
                mCenter + mRadius - inPadding, mCenter + mRadius - inPadding);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        new Thread(new CanvasThread()).start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    private void draw() {
        //当SurfaceView不可编辑或尚未创建，会返回null
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                draw_bg();
                draw_every();
                draw_point();
                mStartAngle += mSpeed;
                if (isShouldEnd) {
                    mSpeed -= 1;
                }
                if (mSpeed < 0) {
                    mSpeed = 0;
                    mHandler.sendEmptyMessage(IS_END);
                    isShouldEnd = false;
                }

            }
        } finally {

            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }


    }

    private static final int IS_END = 0x101;
    /**
     * 转盘停止后要处理的逻辑
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IS_END:
                    implyPraise();
                    break;
                default:
                    break;
            }
        }
    };

    private void implyPraise() {
        if (mWinning != 2 && mWinning != 5) {
            Toast.makeText(getContext(), "恭喜您获得" + mText[mWinning], Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "很遗憾，您未能获得大奖，没关系，再来一次就好！", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }

    public void luckyStart() {
        mSpeed = 60;
        isShouldEnd = false;
    }

    public void luckyEnd(int index) {
        //mStartAngle = 0;
        mWinning = index;
        float start = -mStartAngle % 360 + 5 * 360 + 270 - sweepAngle * (1 + index);
        float range = (float) (start + Math.random() * sweepAngle);
        mSpeed = (float) ((Math.sqrt(8 * range + 1) - 1) / 2);
        isShouldEnd = true;
    }

    private void draw_bg() {
        mPaint.setColor(Color.RED);
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawCircle(mCenter, mCenter, mRadius, mPaint);
    }


    private void draw_every() {
        float startAngle = mStartAngle;
        for (int i = 0; i < mCount; i++) {
            mPaint.setColor(colors[i]);
            mCanvas.drawArc(mRange, startAngle, sweepAngle, true, mPaint);
            draw_text(startAngle, sweepAngle, mText[i]);
            draw_pic(startAngle, i);
            startAngle += sweepAngle;
        }
    }


    private void draw_text(float startAngle, int sweepAngle, String text) {
        Path path = new Path();
        path.addArc(mRange, startAngle, sweepAngle);
        float x = (float) (Math.PI * (mRadius - inPadding) / mCount - textPaint.measureText(text) / 2);
        mCanvas.drawTextOnPath(text, path, x, 2 * inPadding, textPaint);
    }

    private void draw_pic(float startAngle, int id) {
        int picWidth = mRadius >> 3;
        double tempAngle = (startAngle + 360 / mCount / 2) * 2 * Math.PI / 360;
        int x = (int) (mCenter + mRadius / 2 * Math.cos(tempAngle));
        int y = (int) (mCenter + mRadius / 2 * Math.sin(tempAngle));
        Rect rect = new Rect(x - picWidth, y - picWidth, x + picWidth, y + picWidth);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mPic[id]);
        mCanvas.drawBitmap(bitmap, null, rect, null);

    }

    private void draw_point() {
        int picWidth = mRadius >> 4;
        int picHeight = (int) (4 / 5f * mRadius);
        Rect rect = new Rect(mCenter - picWidth, mCenter - picHeight, mCenter + picWidth, mCenter);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.point);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    class CanvasThread implements Runnable {
        @Override
        public void run() {

            while (isRunning) {
                long begin = System.currentTimeMillis();
                draw();
                long end = System.currentTimeMillis();
                if (end - begin < 50) {
                    try {
                        Thread.sleep(50 - (end - begin));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
