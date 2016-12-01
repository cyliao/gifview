package com.smart.gifview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by cyliao on 2016/11/18.
 */

public class GifImageCache {
    private LinkedList<GifFrame> fileList;
    private ArrayList<GifFrame> memList;
    private int size;
    private int maxSize;
    private boolean cacheInFile = false;
    private int frameCount = 0;
    private int cacheMaxFrame = 0;
    private int currentFrameIndex = 0;
    private List<Integer> delayTime = new ArrayList<>();
    private LoadCacheImage cacheImage = new LoadCacheImage();
    private String cacheDir;
    private String fileDir = null;
    private String saveDir = null;
    private Handler mHandler = null;
    private Lock lock = new ReentrantLock(false);
    private boolean decodeFinish = false;
    private int gifWidth;
    private int gifHeight;

    private class LoadCacheImage implements Runnable {
        public void run() {
            int next = cacheMaxFrame + 1;
            if (next >= frameCount && frameCount > 0)
                next = 0;

            GifFrame gif = loadFileImage(next);
            if (gif != null && fileList != null) {
                int s = sizeOf(gif);
                size += s;
                fileList.addLast(gif);
                cacheMaxFrame = gif.index;
            }
        }
    }


    public GifImageCache(int maxSize, String cacheDir, View v) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.cacheDir = cacheDir + File.separatorChar + "gifview";
        if (v != null)
            mHandler = new Handler(v.getContext().getMainLooper());
        File f = new File(this.cacheDir);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    public void setView(View v){
        mHandler =new Handler(v.getContext().getMainLooper());
    }

    public void setFrameCount(int count) {
        lock.lock();
        frameCount = count;
        decodeFinish = true;
        lock.unlock();
    }

    public void setDimen(int width, int height) {
        gifWidth = width;
        gifHeight = height;
    }

    public int getGifWidth() {
        return gifWidth;
    }

    public int getGifHeight() {
        return gifHeight;
    }

    public  int getFrameCount(){
        return frameCount;
    }

    public void save(int index, GifFrame frame) {
        if (frame == null)
            return;
        lock.lock();
        try {
            delayTime.add(index, frame.delay);
            int s = sizeOf(frame);

            if (size + s > maxSize || cacheInFile) {
                if (fileList == null) {
                    fileList = new LinkedList<>();

                    if (memList != null) {
                        ArrayList<GifFrame> tmp = new ArrayList<>();
                        for (int i = 0; i < currentFrameIndex && i < memList.size(); i++) {
                            int si = sizeOf(memList.get(i));
                            size -= si;
                            tmp.add(memList.get(i));
                        }
                        saveMemCache(tmp);
                        for (int i = currentFrameIndex; i < memList.size(); i++) {
                            fileList.addLast(memList.get(i));
                            cacheMaxFrame = i;
                        }
                        Log.d("========", "mem list " + memList.size() + "," + cacheMaxFrame + "," + currentFrameIndex);
                    }
                    cacheInFile = true;
                    memList = null;

                }
                if (size + s > maxSize)
                    toFile(frame);
                else {
                    fileList.addLast(frame);
                    cacheMaxFrame = frame.index;
                }
            } else {
                if (memList == null) {
                    memList = new ArrayList<GifFrame>();
                }
                size += s;
                memList.add(frame);
            }
        } finally {
            lock.unlock();
        }
    }

    private void saveMemCache(final ArrayList<GifFrame> list) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        GifFrame frame = list.get(i);
                        toFile(frame);
                        if (i < list.size() - 1 && frame != null && frame.image != null)
                            list.get(i).image.recycle();
                    }
                }
            }
        };
        new Thread(r).start();
    }

    public GifFrame getNext() {
        GifFrame value = null;
        lock.lock();
        try {
            int index = currentFrameIndex;
            currentFrameIndex++;
            if (currentFrameIndex >= frameCount && frameCount > 0)
                currentFrameIndex = 0;
            if (cacheInFile) {
                value = fileList.pollFirst();

                if (value != null) {
                    size -= sizeOf(value);
                    saveFile(index, value);
                }

                if (value == null) {
                    value = loadFileImage(index);
                }
                if (decodeFinish)
                    mHandler.post(cacheImage);
                return value;
            } else {
                if (memList == null || index >= memList.size()) {
                    currentFrameIndex = index;
                    return null;
                } else
                    return memList.get(index);
            }
        } catch (Exception e){
            Log.e("gif cache getnext",Log.getStackTraceString(e));
            return null;
        }finally {
            lock.unlock();
        }
    }

    private void saveFile(final int index, final GifFrame frame) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                toFile(frame);
            }
        };
        mHandler.post(r);
    }

    protected int sizeOf(GifFrame value) {
        if (value == null || value.image == null)
            return 0;
        int v = Build.VERSION.SDK_INT;
        if (v >= 19) {
            return value.image.getAllocationByteCount();
        } else if (v >= 12) {
            return value.image.getByteCount();
        } else {
            return value.image.getRowBytes() * value.image.getHeight();
        }

    }

    private void toFile(GifFrame v) {
        if (fileDir == null) {
            fileDir = String.valueOf(System.currentTimeMillis());
            saveDir = cacheDir + File.separatorChar + fileDir;
            File d = new File(saveDir);
            if (!d.exists()) {
                d.mkdir();
            }
        }
        File f = new File(saveDir + File.separatorChar + v.index);
        if (f.exists())
            return;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            v.image.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            v.image = null;
        } catch (Exception e) {
            Log.e("gifimagecache", Log.getStackTraceString(e));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private GifFrame loadFileImage(int index) {
        try {
            Log.d("===========", "load image " + index);
            if (index >= delayTime.size())
                return null;
            Bitmap image = BitmapFactory.decodeFile(saveDir + File.separatorChar + index);
            Log.d("==========", index + " image :" + (image == null ? "null" : image.toString()));
            GifFrame gif = new GifFrame(image, delayTime.get(index), index);
            return gif;
        } catch (OutOfMemoryError e) {
            Log.e("gif load", Log.getStackTraceString(e));
            return null;
        } catch (Exception e) {
            Log.e("gif load", Log.getStackTraceString(e));
            return null;
        }
    }

    public void destroy() {
        if (cacheInFile) {
            if (fileList != null) {
                synchronized (this) {
                    while (true) {
                        if (fileList == null || fileList.isEmpty())
                            break;
                        GifFrame frame = fileList.pollFirst();
                        if (frame.image != null)
                            frame.image.recycle();
                    }
                }
            }
            fileList = null;
        } else {
            if (memList != null) {
                synchronized (this) {
                    while (true) {
                        if (memList == null || memList.isEmpty())
                            break;
                        GifFrame frame = memList.remove(0);
                        if (frame.image != null) {
                            frame.image.recycle();
                        }
                    }
                }
            }
            memList = null;
        }

        if (saveDir != null) {
            File dir = new File(saveDir);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files == null)
                    return;
                for (File f : files) {
                    f.delete();
                }
            }
            dir.delete();
        }
        fileDir = null;
        saveDir = null;
    }
}
