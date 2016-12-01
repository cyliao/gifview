package com.smart.gifview.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smart.gifview.GifView;

/**
 * Created by cyliao on 2016/11/18.
 */

public class DemoActivity extends Activity {

    private int[] gifimg = new int[]{R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d};
    private int current = 0;
    private GifView gif;
    private Context context;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.main_demo);
        gif= (GifView)findViewById(R.id.gif);

        Button play = (Button)findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                if(gif.isPause()){
                    gif.continueGifAnimation();
                    b.setText("播放");
                }else{
                    gif.pauseGifAnimation();
                    b.setText("暂停");
                }
            }
        });

        Button next = (Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current++;
                if(current >= gifimg.length){
                    current = 0;
                }
                gif.destroy();
                gif.setGifImage(gifimg[current]);
                gif.start();
            }
        });

        Button demon = (Button)findViewById(R.id.demon);
        demon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(context,DemonDemo.class);
                startActivity(it);
            }
        });
    }



}
