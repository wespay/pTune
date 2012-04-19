package com.ptune.graphics;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DialSurfaceView extends GLSurfaceView {
	
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private DialRenderer mRenderer;
    private float mPreviousX;
    private float mPreviousY;

    public DialSurfaceView(Context context){
    	super(context);
        // set the mRenderer member
        mRenderer = new DialRenderer(context);
        setRenderer(mRenderer);
        
        // Render the view only when there is a change
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }
    
    public DialSurfaceView(Context context, AttributeSet attrs){
    	super(context, attrs);
        // set the mRenderer member
        mRenderer = new DialRenderer(context);
        setRenderer(mRenderer);
        
        // Render the view only when there is a change
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    
    public void update(float frequency) {
    	float target = 220f;
    	float difference = frequency - target;
    	//View parent = (View) this.getParent();
    	//TextView t = (TextView) parent.findViewById(R.id.textView1);
    	//t.setText(Float.toString(frequency));
    	//if (t != null) {
    		//t.setText((CharSequence)"test");
    	//}
    	
    	if (difference < -90) {
    		mRenderer.mAngle = -90f;
    	} else if (difference > 90) {
    		mRenderer.mAngle = 90f;
    	} else {
    		mRenderer.mAngle = difference;
    	}
    	requestRender();
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
    
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
    
                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                  dx = dx * -1 ;
                }
    
                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                  dy = dy * -1 ;
                }
              
                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    } 
}
