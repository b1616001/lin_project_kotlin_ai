package com.example.lin_new_project_kotlin_ai.OpenGLR

import android.opengl.GLSurfaceView
import android.opengl.GLU
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer : GLSurfaceView.Renderer {
    // Initialize our square.
    //private square square=new FlatColoredSquare(); 
    private val square: Square = SmoothColoredSquare()
    private var angle = 0f

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.
         * microedition.khronos.opengles.GL10, javax.microedition.khronos.
         * egl.EGLConfig)
         * 在這個方法中主要用來設置一些繪製時不常變化的參數，比如：背景色，是否打開 z-buffer等
	 */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        //創建或重新創建曲面時調用
        // 將背景顏色設置為黑色( rgba ).
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f) // OpenGL docs.
        // 啟用“平滑著色”，實際上並不需要
        gl.glShadeModel(GL10.GL_SMOOTH) // OpenGL docs.
        //		深度緩衝區設置。
        gl.glClearDepthf(1.0f) // OpenGL docs.
        // 啟用深度測試
        gl.glEnable(GL10.GL_DEPTH_TEST) // OpenGL docs.
        // 進行深度測試的類型
        gl.glDepthFunc(GL10.GL_LEQUAL) // OpenGL docs.
        // 非常好的透視圖計算。
        gl.glHint(
            GL10.GL_PERSPECTIVE_CORRECTION_HINT,  // OpenGL docs.
            GL10.GL_NICEST
        )
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.
         * microedition.khronos.opengles.GL10)
         * 定義實際的繪圖操作。
	 */
    override fun onDrawFrame(gl: GL10) {
        //調用以繪製當前框架
        //清除屏幕和深度緩衝區.

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        // Replace the current matrix with the identity matrix
        gl.glLoadIdentity()
        //將10個單位翻譯到屏幕中。
        gl.glTranslatef(0f, 0f, -10f) //Translate平移變換


        // SQUARE A
        // 保存當前矩陣。.
        gl.glPushMatrix()
        //逆時針旋轉正方形A。
        gl.glRotatef(angle, 0f, 0f, 1f) //旋轉
        // 畫方形A.
        square.draw(gl)
        // 恢復最後一個矩陣。
        gl.glPopMatrix()

        // SQUARE B
        // 保存當前矩陣。
        gl.glPushMatrix()
        // 移動正方形B之前先旋轉它，使其繞A旋轉.
        gl.glRotatef(-angle, 0f, 0f, 1f) //旋轉
        // Move square B.
        gl.glTranslatef(2f, 0f, 0f) //平移變換
        // Scale it to 50% of square A
        gl.glScalef(.5f, .5f, .5f)
        // Draw square B.
        square.draw(gl)

        // SQUARE C
        // 保存當前矩陣。
        gl.glPushMatrix()
        // Make the rotation around B
        gl.glRotatef(-angle, 0f, 0f, 1f) //旋轉
        gl.glTranslatef(2f, 0f, 0f) //平移變換
        // Scale it to 50% of square B
        gl.glScalef(.5f, .5f, .5f)
        // Rotate around it's own center.
        gl.glRotatef(angle * 10, 0f, 0f, 1f)
        // Draw square C.
        square.draw(gl)

        // Restore to the matrix as it was before C.
        gl.glPopMatrix()
        // Restore to the matrix as it was before B.
        gl.glPopMatrix()

        // Increse the angle.
        angle++
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.
         * microedition.khronos.opengles.GL10, int, int)
         * 如果設備支持屏幕橫向和縱向切換，這個方法將發生在橫向<->縱向互換時。此時可以重新設置繪製的縱橫比率。
	 */
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
//        /**/當表面更改大小時調用。 */
        //將當前視口設置為新大小。.
        gl.glViewport(0, 0, width, height) // OpenGL docs.
        //選擇投影矩陣
        gl.glMatrixMode(GL10.GL_PROJECTION) // OpenGL docs.
        // 重置投影矩陣
        gl.glLoadIdentity() // OpenGL docs.
        // 計算窗口的寬高比
        GLU.gluPerspective(
            gl, 45.0f,
            width.toFloat() / height.toFloat(),
            0.1f, 100.0f
        )
        //選擇模型視圖矩陣
        gl.glMatrixMode(GL10.GL_MODELVIEW) // OpenGL docs.
        //重置模型視圖矩陣
        gl.glLoadIdentity() // OpenGL docs.
    }
}
