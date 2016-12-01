package com.smart.gifview.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.gifview.GifImageType;
import com.smart.gifview.GifListener;
import com.smart.gifview.GifView;

public class MainActivity extends AppCompatActivity implements GifListener {

    private GifView gifView = null;

    private LinearLayout layout = null;

    private int currentType = 1;

    private TextView desc = null;

    private TextView frame_desc = null;

    private ImageView img = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_static = (Button) findViewById(R.id.static_btn);
        btn_static.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(currentType != 1){
                    setGifView(1);
                }
            }
        });

        Button btn_one = (Button)findViewById(R.id.one_btn);
        btn_one.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(currentType != 2){
                    setGifView(2);
                }
            }
        });

        Button btn_loop = (Button)findViewById(R.id.loop_btn);
        btn_loop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(currentType != 3){
                    setGifView(3);
                }
            }
        });

        layout = (LinearLayout)findViewById(R.id.gif_show);
        layout.setBackgroundColor(0xFFFF00);
        desc = (TextView)findViewById(R.id.desc);

        frame_desc = (TextView)findViewById(R.id.frame_desc);

        setGifView(1);
    }

    private void destroyGif(){
        gifView = null;
    }

    private void newGifView(){
        destroyGif();
        gifView = new GifView(this);
        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gifView.isPause())
                    gifView.continueGifAnimation();
                else
                    gifView.pauseGifAnimation();
            }
        });
        LayoutParams l = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        gifView.setLayoutParams(l);
        gifView.setScaleType(ScaleType.FIT_CENTER);
    }

    private void setGifView(int type){
        if(type >=1 && type <=3){
            newGifView();
            switch(type){
                case 1:
                    gifView.setGifImage(R.drawable.d);
                    frame_desc.setText("");
                    break;
                case 2:
                    gifView.setGifImage(R.drawable.a);
                    gifView.setBackgroundColor(0xFFFF00);
                    gifView.setLoopCount(2);
                    gifView.setGifListener(this);
                    frame_desc.setText("");
                    break;
                case 3:
                    gifView.setGifImage(R.drawable.c);
                    gifView.setLoopStatus(true);
                    gifView.setGifListener(this);
                    gifView.setGifImageType(GifImageType.SYNC_DECODER);
                    frame_desc.setText("");
                    break;
            }
            currentType = type;
            layout.removeAllViews();
            layout.addView(gifView);
        }
    }

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

    }

    @Override
    public void gifDimenDecode(int width, int height) {

    }
}
