package com.smart.gifview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by cyliao on 2016/11/17.
 */

public class GifView extends ImageView {
    private int defaultMemSize = 5 * 1204 * 1204;
    /**
     * gif解码器
     */
    private GifDecoder gifDecoder = null;
    /**
     * 当前要画的帧的图
     */
    private Bitmap currentImage = null;
    private int currentIndex = 0;
    private GifAnimation animation = null;

    private boolean animationRun = false;

    private GifImageCache cache = null;

    private int currentLoop = 0;

    private int frameCount = 0;

    private GifListener callback = null;
    private int cacheSize = defaultMemSize;

    private int loopCount = 0;
    /**
     * 是否循环播放
     */
    private boolean loopPlay = true;

    private int gifStatus = 0;
    private int gifWidth ;
    private int gifHeight;

    private boolean autoSize = false;
    private boolean decodeOom = false;


    private GifImageType animationType = GifImageType.COVER;

    private GifDecodeListener decodeListener = new GifDecodeListener() {
        @Override
        public void firstFrameDecoded(GifFrame frame) {
            if (getVisibility() == GONE || getVisibility() == INVISIBLE || gifStatus == 1) {
                return;
            }
            setCurrentFrame(frame);
            animation.reDraw();
            if(callback != null){
                callback.firstFrameDecode();
            }
        }

        @Override
        public void decodeFinish(boolean success, int frame) {
            if (getVisibility() == GONE || getVisibility() == INVISIBLE || gifStatus == 1) {
                return;
            }
            if (!success)
                return;
            frameCount = frame;
            if (frameCount == 1) {
                stopDrawThread();
                stopDecodeThread();
            } else {
                if (animationRun == false) {
                    animation.goon();
                    animationRun = true;
                }
            }
            if(callback != null){
                callback.decodeFinish(success);
            }
        }

        @Override
        public void frameDecode(int index, GifFrame frame) {
            if (getVisibility() == GONE || getVisibility() == INVISIBLE || gifStatus == 1) {
                return;
            }
            if (animationType == GifImageType.SYNC_DECODER) {
                animation.goon();
            }
            if(callback != null){
                callback.decodeFrame(index);
            }
        }

        @Override
        public void gifDimenDeocde(int width, int height) {
            gifWidth = width;
            gifHeight = height;
            if(callback != null){
                callback.gifDimenDecode(width,height);
            }
        }

        @Override
        public void decodeOOM() {
            decodeOom = true;
            pauseGifAnimation();
        }
    };

    public GifView(Context context) {
        super(context);
        setScaleType(ImageView.ScaleType.FIT_XY);

    }

    public GifView(Context context, AttributeSet attrs){
        super(context,attrs);
        loadConfig(context,attrs,0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);

        loadConfig(context,attrs,defStyle);
    }

    private void loadConfig(Context context,AttributeSet attrs, int defStyle){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GifView, defStyle, 0);
        int srcid = a.getResourceId(R.styleable.GifView_gif_src,0);
        cacheSize = a.getInt(R.styleable.GifView_cache_size,defaultMemSize);
        loopCount = a.getInt(R.styleable.GifView_loop_count,0);
        loopPlay = a.getBoolean(R.styleable.GifView_loop_play,true);
        autoSize = a.getBoolean(R.styleable.GifView_auto_size,false);
        int type = a.getInt(R.styleable.GifView_decode_type,1);
        a.recycle();
        switch (type){
            case 0:
                animationType = GifImageType.WAIT_FINISH;
                break;
            case 2:
                animationType = GifImageType.COVER;
                break;
            default:
                animationType = GifImageType.SYNC_DECODER;
                break;
        }

        setScaleType(ImageView.ScaleType.FIT_XY);
        animation = new GifAnimation(this);

        if(srcid != 0){
            setGifImage(srcid);
        }
    }

    /**
     * 设置gif listener
     * @param listener
     */
    public void setGifListener(GifListener listener) {
        this.callback = listener;
    }

    /**
     * 设置播放次数
     * @param count
     */
    public void setLoopCount(int count) {
        loopCount = count;
    }

    /**
     * 设置动画是否循环播放<br>
     * 默认动画循环播放
     */
    public void setLoopStatus(boolean loop) {
        loopPlay = loop;
    }

    public boolean isPause(){
        return animation.isPause();
    }

    /**
     * 清理，不使用的时候，调用本方法来释放资源
     */
    public void destroy() {
        gifStatus = 1;
        frameCount = 0;
        currentIndex = 0;
        currentLoop = 0;
        gifWidth = 0;
        gifHeight = 0;
        loopCount = 0;
        currentImage = null;
        stopDecodeThread();
        stopDrawThread();
        animation.destroy();
        cache.destroy();
        gifDecoder = null;
        animation = null;
    }

    /**
     * 继续显示动画。当动画暂停后，通过本方法来使动画继续
     */
    public void continueGifAnimation() {
        if (frameCount == 1)
            return;
        if(decodeOom && frameCount == 0)
            return;
        animation.goon();
    }

    /**
     * 暂停动画<br>
     * 建议在onpause时，调用本方法
     */
    public void pauseGifAnimation() {
        if (frameCount == 1)
            return;
        animation.pause();
    }

    /**
     * 设置gif在解码过程中的显示方式<br>
     *
     * @param type 显示方式
     */
    public void setGifImageType(GifImageType type) {
        animationType = type;
    }

    private void reAnimation() {
        if (frameCount == 1)
            return;
        stopDrawThread();
        currentLoop = 0;
        animation.play();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            stopDrawThread();
        } else if (visibility == VISIBLE) {
            reAnimation();
        }
    }

    private void init() {
        gifStatus = 0;
        animation = new GifAnimation(this);
        stopDrawThread();
        if (currentImage != null) {
            currentImage = null;
        }
        if (gifDecoder != null) {
            stopDecodeThread();
            gifDecoder = null;
        }
        currentLoop = 0;
        cache = new GifImageCache(cacheSize, getContext().getCacheDir().getAbsolutePath(), this);

        gifDecoder = new GifDecoder(cache, decodeListener);
    }

    /**
     * 中断解码线程
     */
    private void stopDecodeThread() {
        if (gifDecoder != null && gifDecoder.getState() != Thread.State.TERMINATED) {
            gifDecoder.cancel();
            gifDecoder.interrupt();
        }
    }

    /**
     * 中断动画线程
     */
    private void stopDrawThread() {
        if (frameCount == 1)
            return;
        animation.stop();
        animationRun = false;
    }

    /**
     * 设置图片，并开始解码
     *
     * @param gif 要设置的图片
     */
    public void setGifImage(byte[] gif) {
        init();
        gifDecoder.setGifImage(gif);
    }

    public void start(){
        if(gifDecoder != null)
            gifDecoder.start();
    }

    public void setGifImage(int resId) {
        init();
        gifDecoder.setGifImage(getResources(), resId);
    }

    /**
     * 以文件形式设置gif图片
     *
     * @param strFileName gif图片路径，此图片必须有访问权限
     */
    public void setGifImage(String strFileName) {
        init();
        gifDecoder.setGifImage(strFileName);
    }

    public int draw() {
        int delay = getCurrentFrame();
        drawImage();
        return delay;
    }

    public void drawImage() {
        if (currentImage == null || (currentImage != null && currentImage.isRecycled() == false)) {
            setImageBitmap(currentImage);
            invalidate();
            if (currentIndex >= frameCount && frameCount > 0) {
                currentIndex = 0;
                currentLoop++;
                if (callback != null) {
                    callback.playFinish(currentLoop);
                }
                if(loopCount > 0 && currentLoop >= loopCount){
                    animation.stop();
                }
                if(!loopPlay)
                    animation.stop();
            }
        }
    }

    private int setCurrentFrame(GifFrame frame) {
        currentImage = frame.image;
        currentIndex++;
        return frame.delay;
    }

    private int getCurrentFrame() {
        GifFrame frame = cache.getNext();
        if (frame == null) {
            return -1;
        }
        if (frame.image != null) {
            currentImage = frame.image;
        }
        currentIndex++;
        return frame.delay;
    }

    public void invalidateView() {
        if(animation != null)
            animation.reDraw();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(gifDecoder != null)
            gifDecoder.start();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private int measureWidth(int measureSpec) {
        int specSize = MeasureSpec.getSize(measureSpec);

        return specSize;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(!autoSize) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if(gifWidth == 0 || gifHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int w = measureWidth(widthMeasureSpec);

        float f = (w * 1.0f) / gifWidth;
        int h = (int)(gifHeight * f);
        setMeasuredDimension(w, h);

    }

    public int getGifWidth(){
        return cache.getGifWidth();
    }

    public int getGifHeight(){
        return cache.getGifHeight();
    }

    public void setCacheAndStart(GifImageCache cache){
        destroy();
        gifStatus = 0;
        animation = new GifAnimation(this);
        this.cache = cache;
        this.cache.setView(this);
        frameCount = cache.getFrameCount();
        animation.play();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView,visibility);
        setVisibility(visibility);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        setVisibility(visibility);
    }

}
