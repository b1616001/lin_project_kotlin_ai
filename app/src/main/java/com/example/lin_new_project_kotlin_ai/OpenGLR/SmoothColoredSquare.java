package com.example.lin_new_project_kotlin_ai.OpenGLR;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SmoothColoredSquare extends Square{

	// The colors mapped to the vertices.
    float[] colors = {
            1f, 0f, 0f, 1f, // vertex 0 red
            0f, 1f, 0f, 1f, // vertex 1 green
            0f, 0f, 1f, 1f, // vertex 2 blue
            1f, 0f, 1f, 1f, // vertex 3 magenta
    };

    // Our color buffer.
	private FloatBuffer colorBuffer;

    
	public SmoothColoredSquare(){
		super();
		// float has 4 bytes, colors (RGBA) * 4 bytes
		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		colorBuffer = cbb.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);

	}
	/**
	 * This function draws our square on screen.
	 * @param gl
	 */
	public void draw(GL10 gl) {
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		// 啟用顏色數組緩衝區為 在渲染期間使用.
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY); // NEW LINE ADDED.
		// 指出顏色緩衝區的位置
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer); // NEW LINE ADDED.

		super.draw(gl);
		// Disable the color buffer.
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

	}
}

