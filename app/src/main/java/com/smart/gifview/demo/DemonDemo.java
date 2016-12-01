package com.smart.gifview.demo;

import android.app.Activity;
import android.os.Bundle;

import com.smart.gifview.GifDecodeDemon;
import com.smart.gifview.GifImageCache;
import com.smart.gifview.GifListener;
import com.smart.gifview.GifView;

/**
 * Created by cyliao on 2016/11/30.
 */

public class DemonDemo extends Activity {
    private int[] img = new int[]{R.drawable.b, R.drawable.c, R.drawable.d};
    private GifImageCache[] imgCache = new GifImageCache[4];
    private GifView gif;
    private int currentIndex = 0;
    private boolean lastPalyFinish = true;
    private int playIndex = 0;

    private GifDecodeDemon.IDemonDecodeFinish demonDecodeFinish = new GifDecodeDemon.IDemonDecodeFinish() {
        @Override
        public void decodeFinish(GifImageCache cache) {
            if (lastPalyFinish) {
                lastPalyFinish = false;
                playIndex++;
                gif.setCacheAndStart(cache);
                gif.setLoopCount(1);
            }else
                imgCache[playIndex + 1] = cache;
            currentIndex++;
            if (currentIndex < img.length) {
                new Decode().start();
            }
        }
    };

    private GifListener listener = new GifListener() {
        @Override
        public void firstFrameDecode() {

        }

        @Override
        public void decodeFinish(boolean success) {

        }

        @Override
        public void decodeFrame(int index) {

        }

        @Override
        public void playFinish(int loop) {
            lastPalyFinish = true;
            if (playIndex < 3) {
                if (imgCache[playIndex + 1] != null) {
                    gif.setCacheAndStart(imgCache[playIndex + 1]);
                    gif.setLoopCount(1);
                    lastPalyFinish = false;
                    playIndex++;
                }
            }
        }

        @Override
        public void gifDimenDecode(int width, int height) {

        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_demo);
        gif = (GifView) findViewById(R.id.gif);
        gif.setGifListener(listener);
        new Decode().start();
    }

    private class Decode extends Thread {
        public void run() {
            GifDecodeDemon d = new GifDecodeDemon(2097152, getCacheDir().getAbsolutePath());
            d.setDecodeListener(demonDecodeFinish);
            d.setGifImage(getResources(), img[currentIndex]);
            d.decode();
        }
    }

}
