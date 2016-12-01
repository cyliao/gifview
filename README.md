# android gifview
---
##用法
~~~
<com.smart.gifview.GifView
        android:id="@+id/gif"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:cache_size="2097152"
        app:decode_type="sync_decode"
        app:gif_src="@drawable/a"
        app:loop_play="true"
        app:loop_count="1"/>
~~~
- cache_size : 缓存大小(byte)
- gif_src : gif图片
- decode_type : 解码时的显示方式，wait_finish等待解码结束再播放，sync_decode边解码边播放，cover解码时显示第一帧，解码结束再播放
- loop_play : 是否循环播放，默认true
- loop_count : 播放次数
- auto_size : 是否自动调整画面大小，默认false
###在退出使用时，最好调用destory来释放

---
##关于OOM

- 调整缓存大小
- 如果gif图片太大，最好把解码显示方式设为cover或wait_finish
- 如果可以先解码后显示，可以用预解码的方式

##其它
gifview其实在几年以前写的，原来放在https://code.google.com/archive/p/gifview2，这次重新写了一次，放到git上
