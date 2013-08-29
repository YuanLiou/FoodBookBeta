package com.example.mysql_tester.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ImageViewHelper {
	Boolean isZoomed = false;
	ImageView imageView;
	Bitmap bitmap;
	Context context;
	private float originalX, originalY;
	private float minScaleR;    //最小縮放比例
	private Matrix matrix = new Matrix();
	private Matrix saved_matrix = new Matrix();
	private static final int NONE = 0;    //初始值
	private static final int DRAG = 1;    //拖曳
	private static final int ZOOM = 2;    //縮放
	private DisplayMetrics dm;
	private int mode = NONE;
	private PointF prev = new PointF();
	private PointF mid = new PointF();
	private float dist = 1f;
    GestureDetector gestureDetector;
    public ImageViewHelper(Context context,DisplayMetrics dm,ImageView imageView,Bitmap bitmap) {
    	this.dm = dm;
    	this.imageView = imageView;
    	this.bitmap = bitmap;
    	this.context = context;
    	setImageSize();
    	//minZoom();
    	center();
    	imageView.setImageMatrix(matrix);
    	originalX = bitmap.getWidth();
    	originalY = bitmap.getHeight();
    }
    
    //偵測雙擊
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (isZoomed) {
				Matrix originalMartrix = new Matrix();
				originalMartrix.postScale(originalX, originalY);
				imageView.setImageMatrix(originalMartrix);
				imageView.setScaleType(ScaleType.FIT_CENTER);
				isZoomed = false;
				//Log.e("Touch event Log", "Action Double Tap");
			}
			return true;
		}
    }
    
    public Matrix getMatrix() {
    	return matrix;
    }
    
    public void center() {
    	center(true,true);
    }
    
    //跳出螢幕範圍回中心點
    public void center(boolean horizontal,boolean vertical) {
    	Matrix m = new Matrix();
    	m.set(matrix);
    	RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	m.mapRect(rect);
    	float height = rect.height();
    	float width = rect.width();
    	float delta_x = 0, delta_y = 0;
    	//圖片小於螢幕大小，則置中
    	//大於螢幕，上方留空白則往上移，下方留空白則往下移
    	if (vertical) {
    		int screen_height = dm.heightPixels;
    		if (height < screen_height) {
    			delta_y = (screen_height - height) / 2 - rect.top;
    		}
    		else if (rect.top > 0) {
				delta_y = -rect.top;
			} 
    		else if (rect.bottom < screen_height) {
				delta_y = imageView.getHeight() - rect.bottom;
			}
    	}
    	if (horizontal) {
    		int screen_width = dm.widthPixels;
    		if (width < screen_width) {
    			delta_x = (screen_width - width) / 2 - rect.left;
    		}
    		else if (rect.left > 0) {
    			delta_x = -rect.left;
    		}
    		else if (rect.right < screen_width) {
    			delta_x = screen_width - rect.right;
    		}
    	}
    	matrix.postTranslate(delta_x, delta_y);
    }
    
    //監聽觸控事件,單指: 拖動, 雙指: 縮放
    public void setImageSize() {
    	gestureDetector = new GestureDetector(context, new GestureListener());
    	imageView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);    //啟用雙擊
                LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					saved_matrix.set(matrix);
					prev.set(event.getX(), event.getY());
					mode = DRAG;
					//Log.e("Touch event Log", "Action Down");
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					dist = spacing(event);
					//如果兩點的距離超過10, 就判斷為多點觸控 -- 縮放模式
					if (spacing(event) > 10f) {
						saved_matrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
						//Log.e("Touch event Log", "Action Pointer Down");
					}
					break;
				case MotionEvent.ACTION_UP:
					//Log.e("Touch event Log", "Action Up");
					mode = NONE;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					//Log.e("Touch event Log", "Action Pointer Up");
					mode = NONE;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG && isZoomed) {
						matrix.set(saved_matrix);
						matrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
					}
					else if (mode == ZOOM) {
						float newDist = spacing(event);    //偵測兩跟手指移動的距離
						if (newDist > 10f) {
							matrix.set(saved_matrix);
							float tScale = newDist / dist;
							matrix.postScale(tScale, tScale, mid.x, mid.y);
							imageView.setLayoutParams(vp);
							isZoomed = true;
						}
					}
					//Log.e("Touch event Log", "Action Move");
					break;
				}
                if (isZoomed) imageView.setScaleType(ScaleType.MATRIX);
				imageView.setImageMatrix(matrix);
				center();
				return true;
			}
		});
    }
    
    //兩點的距離
    public float spacing(MotionEvent event) {
    	float x = event.getX(0) - event.getX(1);
    	float y = event.getY(0) - event.getY(1);
    	float result = (float)Math.sqrt(x * x + y * y);
    	return result;
    }
    
    //兩點的中點
    public void midPoint(PointF point, MotionEvent event) {
    	float x = event.getX(0) + event.getX(1);
    	float y = event.getY(0) + event.getY(1);
    	point.set(x / 2, y / 2);
    }
    //取得最小的比例，假設圖片比螢幕大: 則螢幕(寬/長) / 圖片(寬/長)會小於1 -- 將圖片進行縮小
    //反之進行放大，圖片越小，放大倍數越大，如果螢幕跟圖片大小相同倍數為1,不變
    public void minZoom() {
        minScaleR = Math.min(
        		(float)dm.widthPixels / (float)bitmap.getWidth(),
        		(float)dm.heightPixels / (float)bitmap.getHeight());
    	if (minScaleR <= 1.0) {
    		matrix.postScale(minScaleR, minScaleR);
    	}
    	else {
    		matrix.postScale(1.5f, 1.5f);
    	}
    }
}
