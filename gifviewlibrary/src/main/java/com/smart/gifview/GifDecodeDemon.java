package com.smart.gifview;

import android.content.res.Resources;

/**
 * Created by cyliao on 2016/11/30.
 */

public class GifDecodeDemon {

    public interface IDemonDecodeFinish{
        public void decodeFinish(GifImageCache cache);
    }

    private GifDecoder gifDecoder;
    private GifImageCache cache;
    private boolean decodeFinish = false;
    private int size;
    private String cacheDir;

    private GifDecodeListener listener = new GifDecodeListener() {
        @Override
        public void firstFrameDecoded(GifFrame frame) {

        }

        @Override
        public void decodeFinish(boolean success, int frameCount) {
            if(success) {
                decodeFinish = true;
                if (decodeCallback != null)
                    decodeCallback.decodeFinish(cache);
            }
        }

        @Override
        public void frameDecode(int index, GifFrame frame) {

        }

        @Override
        public void gifDimenDeocde(int width, int height) {

        }

        @Override
        public void decodeOOM() {

        }
    };
    private IDemonDecodeFinish decodeCallback = null;

    public GifDecodeDemon(int cacheSize,String cacheDir){
        size = cacheSize;
        this.cacheDir = cacheDir;
    }

    private void init(){
        cache = new GifImageCache(size,cacheDir,null);
        gifDecoder = new GifDecoder(cache,listener);
    }


    public void setGifImage(byte[] gif) {
        init();
        gifDecoder.setGifImage(gif);
    }

    public void setGifImage(Resources res, int resId) {
        init();
        gifDecoder.setGifImage(res, resId);
    }

    public void setGifImage(String strFileName) {
        init();
        gifDecoder.setGifImage(strFileName);
    }


    public void decode(){
        if(gifDecoder != null)
            gifDecoder.start();
    }

    public GifImageCache getCache(){
        if(decodeFinish)
            return cache;
        return null;
    }

    public void setDecodeListener(IDemonDecodeFinish listener){
        decodeCallback = listener;
    }
}
