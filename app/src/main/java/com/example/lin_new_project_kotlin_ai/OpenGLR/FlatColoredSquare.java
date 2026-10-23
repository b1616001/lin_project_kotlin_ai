package com.example.lin_new_project_kotlin_ai.OpenGLR;

import javax.microedition.khronos.opengles.GL10;

public class FlatColoredSquare extends Square{

	public FlatColoredSquare(){
		super();
	}
	/**
	 * This function draws our square on screen.
	 * @param gl
	 */
	public void draw(GL10 gl) {
		gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
		super.draw(gl);
	}
}
