package com.ptune.graphics;

import com.ptune.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DialView extends SurfaceView implements SurfaceHolder.Callback {
	
	class DialThread extends Thread {
		
		private SurfaceHolder mSurfaceHolder;
		private Context mContext;
		private Handler mHandler;
		private boolean isRunning = false;
		private float mValue = -90;
		private float mPos = -90;
		
		/** The background image. */
		private Bitmap mBackgroundImage;
		
		public DialThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			mSurfaceHolder = surfaceHolder;
			mHandler = handler;
			mContext = context;
			
			mBackgroundImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.dial);
			//mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, 611, 321, true);
		} // End DialTrhread constructor.
		
		@Override
		public void run() {
			while(isRunning) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas();
					synchronized (mSurfaceHolder) {
						doDraw(c);
						this.sleep(1);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
		
		private void doDraw(Canvas canvas) {
			Paint linePaint = new Paint();
			linePaint.setAntiAlias(true);
			linePaint.setARGB(255, 200, 200, 255);
			linePaint.setStrokeWidth(4f);
			
			// Draw background image
			canvas.drawBitmap(mBackgroundImage, 0, 0, null);
			
			// Draw needle
			float startX = canvas.getWidth()/2 - 2;
			float stopX = canvas.getWidth()/2 - 2;
			float startY = canvas.getHeight() * 0.93f;
			float stopY = canvas.getHeight() * 0.1f;
			updatePosition();
			canvas.save();
			//canvas.rotate(mValue, startX, startY);
			canvas.rotate(mPos, startX, startY);
			canvas.drawLine(startX, startY, stopX, stopY, linePaint);
			canvas.restore();
		}
		
		private void updatePosition() {
			if (Math.abs(mValue - mPos) > 2) {
				if (mValue > mPos)
					mPos += 2f;
				if (mValue < mPos)
					mPos -= 2f;
			} else {
				mPos = mValue;
			}
		}
		
		public void setSurfaceSize(int width, int height) {
			synchronized(mSurfaceHolder) {
				// TODO: It might be nice to store these values.
				// TODO: Center the image in the available space.
				
				/*
				 * Fit the background image to the width of the surface unless the surface
				 * width is greater than twice the height, in this case fit the background
				 * image to the height.
				 */
				if (width > height*2) {
					mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, height*2, height, true);
				} else {
					mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, width/2, true);
				}
			}
		}
		
		public void setRunning(boolean b) {
			isRunning = b;
		}
		
		public void setValue(float f) {
			if (f < -90)
	    		mValue = -90;
			else if (f > 90)
	    		mValue = 90;
			else
				mValue = f;
		}
	} // End DialThread class.

	private DialThread mThread;
	
	public DialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		mThread = new DialThread(holder, context, new Handler());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mThread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mThread.setRunning(true);
		mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = false;
		mThread.setRunning(false);
		while (retry) {
			try {
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// nothing
			}
		}
	}

	public void update(float value) {
		mThread.setValue(value);
	}
}
