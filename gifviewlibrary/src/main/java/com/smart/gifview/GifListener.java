package com.smart.gifview;

/**
 * Created by cyliao on 2016/11/17.
 */

public interface GifListener {
    /**第一帧解决成功*/
    public void firstFrameDecode();
    /**
     * gif解码结束
     * @param success 是否解决成功
     */
    public void decodeFinish(boolean success);

    /**
     * 每帧解码
     * @param index
     */
    public void decodeFrame(int index);

    /**
     * 每轮播放回调
     * @param loop
     */
    public void playFinish(int loop);

    public void gifDimenDecode(int width,int height);
}
