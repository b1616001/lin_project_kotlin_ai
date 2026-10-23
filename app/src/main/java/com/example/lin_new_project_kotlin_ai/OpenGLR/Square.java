package com.example.lin_new_project_kotlin_ai.OpenGLR;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Square {
	// Our vertices.
	protected float vertices[] = {
		      -1.0f,  1.0f, 0.0f,  // 0, Top Left
		      -1.0f, -1.0f, 0.0f,  // 1, Bottom Left
		       1.0f, -1.0f, 0.0f,  // 2, Bottom Right
		       1.0f,  1.0f, 0.0f,  // 3, Top Right
		};

	// The order we like to connect them.
	protected short[] indices = { 0, 1, 2, 0, 2, 3 };

	// Our vertex buffer.
	protected FloatBuffer vertexBuffer;

	// Our index buffer.
	protected ShortBuffer indexBuffer;

	public Square() {
		// a float is 4 bytes, therefore we 
		// multiply the number if
		// vertices with 4.
		ByteBuffer vbb 
		  = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		// short is 2 bytes, therefore we multiply 
		//the number if
		// vertices with 2.
		ByteBuffer ibb 
		 = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	/**
	 * This function draws our square on screen.
	 * @param gl
	 */
	public void draw(GL10 gl) {
		// 逆時針方法為面
		gl.glFrontFace(GL10.GL_CCW); 
		// 打開 忽略「後面」設置
		gl.glEnable(GL10.GL_CULL_FACE); 
		// 明確指明「忽略「哪個面的代碼如下.
		gl.glCullFace(GL10.GL_BACK);
		//啟用頂點緩衝區以進行寫入並在渲染期間使用。
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//指定頂點數組的位置和數據格式
		// 渲染時使用的坐標。
		//完成緩衝區操作後，不要忘記禁用它。
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 
                                 vertexBuffer);
		//可以重新定義頂點的順序，頂點的順序由indices Buffer 指定。
		gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
				  GL10.GL_UNSIGNED_SHORT, indexBuffer);

		//禁用頂點緩衝區。
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		// 禁用臉部剔除。 Disable face culling.
		gl.glDisable(GL10.GL_CULL_FACE); 
	}

}
