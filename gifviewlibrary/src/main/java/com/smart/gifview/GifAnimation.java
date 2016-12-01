package com.smart.gifview;

import android.os.Handler;

public class GifAnimation {

    private boolean pause = false;
    private GifView view = null;

    private Handler handler = null;
    private AnimationRunAble animation = new AnimationRunAble();


    public GifAnimation(GifView v) {
        handler = new Handler(v.getContext().getMainLooper());
        view = v;
    }

    public synchronized boolean isPause() {
        return pause;
    }

    public synchronized void pause() {
        handler.removeCallbacks(animation);
        pause = true;
    }

    public synchronized void rePlay() {
        pause = false;
        handler.post(animation);
    }

    public synchronized void stop() {
        pause();
    }

    public synchronized void play() {
        pause = false;
        handler.post(animation);
    }

    public synchronized void reDraw() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                view.drawImage();
            }
        });
    }

    public synchronized void goon() {
        if (pause) {
            pause = false;
            handler.post(animation);
        }
    }

    public void destroy() {
        stop();
    }

    private class AnimationRunAble implements Runnable {
        public void run() {
            if (!pause) {
                int delay = view.draw();
                if (delay >= 0)
                    handler.postDelayed(animation, delay);
                else
                    pause = true;
            }
        }
    }

}
