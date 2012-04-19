package com.ptune.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

public class DialRenderer implements Renderer {

	private Context context;
	private DialFace face;
	private FloatBuffer triangleVB;
	public float mAngle;

	/** Constructor to set the handed over context */
	public DialRenderer(Context context) {
		this.context = context;
		this.face = new DialFace();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Set the background frame color
		//gl.glClearColor(0f, 0f, 0f, 1.0f);
		
		// Load the texture for the square
		face.loadGLTexture(gl, this.context);

		// initialize the triangle vertex array
		initShapes();

		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do

		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// Redraw background color
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Set GL_MODELVIEW transformation mode
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();   // reset the matrix to its default state

		// When using GL_MODELVIEW, you must set the view point
		GLU.gluLookAt(gl, 0, 0, -1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		face.draw(gl);

		// Create a rotation for the triangle (Boring! Comment this out:)
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
		
		gl.glTranslatef(0f, -0.5f, 0f);

        // Use the mAngle member as the rotation value
        gl.glRotatef(mAngle, 0.0f, 0.0f, 1.0f);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// Draw the triangle
		gl.glColor4f(0.63671875f, 0.76953125f, 0.22265625f, 0.0f);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triangleVB);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		
		gl.glColor4f(1f, 1f, 1f, 0.0f); // reset color
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		//gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		// make adjustments for screen ratio
		float ratio = (float) width / height;
		
		gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
		gl.glLoadIdentity();                        // reset the matrix to its default state
		
		// apply the projection matrix
		if (ratio > 1.9f) {
			//gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
			//float temp = (float) height / width;
			gl.glFrustumf(-(ratio*0.5f), ratio*0.5f, -0.5f, 0.5f, 1, 10);
		} else {
			ratio = (float) height / width;
			gl.glFrustumf(-1, 1, -ratio, ratio, 1, 10);
		}

	}

	private void initShapes(){

		float triangleCoords[] = {
				// X, Y, Z
				-0.01f, 0f, 0,
				0.01f, 0f, 0,
				0.0f,  1f, 0
		}; 

		// initialize vertex Buffer for triangle  
		ByteBuffer vbb = ByteBuffer.allocateDirect(
				// (# of coordinate values * 4 bytes per float)
				triangleCoords.length * 4); 
		vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
		triangleVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
		triangleVB.put(triangleCoords);    // add the coordinates to the FloatBuffer
		triangleVB.position(0);            // set the buffer to read the first coordinate

	}
}
