package com.smart.gifview;

import android.graphics.Bitmap;

public class GifFrame {
	/**
	 * 构造函数
	 * @param im 图片
	 * @param del 延时
	 */
	public GifFrame(Bitmap im, int del,int index) {
		image = im;
		delay = del;
		this.index = index;
	}
	
	/**图片*/
	public Bitmap image;
	/**延时*/
	public int delay;

	public int index;
	
}
