package com.smart.gifview;

/**
 * Created by cyliao on 2016/11/17.
 */

public interface GifDecodeListener {
    public void firstFrameDecoded(GifFrame frame);
    public void decodeFinish(boolean success,int frameCount);
    public void frameDecode(int index,GifFrame frame);
    public void gifDimenDeocde(int width,int height);
    public void decodeOOM();
}
