package com.example.lin_new_project_kotlin_ai.OpenGLR

import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class GlSceneMode(val label: String) {
    ORBIT_SQUARES("軌道方塊"),
    CUBE_3D("3D 立方體"),
    FLAT_MATRIX("幾何陣列")
}

enum class GlBgTheme(val label: String, val r: Float, val g: Float, val b: Float) {
    SPACE_DARK("深邃太空", 0.05f, 0.07f, 0.12f),
    PURE_BLACK("經典純黑", 0.0f, 0.0f, 0.0f),
    DEEP_TEAL("極光深青", 0.03f, 0.16f, 0.18f)
}

data class GlTelemetry(
    val fps: Int,
    val vendor: String,
    val renderer: String,
    val version: String,
    val viewportWidth: Int,
    val viewportHeight: Int,
    val pitchDeg: Float,
    val yawDeg: Float,
    val zoomDistance: Float
)

class OpenGLRenderer : GLSurfaceView.Renderer {

    private val smoothSquare: Square = SmoothColoredSquare()
    private val flatSquare: Square = FlatColoredSquare()
    private val colorCube = ColorCube()

    private val squareOutlineBuffer: FloatBuffer = ByteBuffer.allocateDirect(4 * 3 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(
                floatArrayOf(
                    -1f, 1f, 0f,
                    -1f, -1f, 0f,
                    1f, -1f, 0f,
                    1f, 1f, 0f
                )
            )
            position(0)
        }

    @Volatile
    var sceneMode: GlSceneMode = GlSceneMode.CUBE_3D

    @Volatile
    var bgTheme: GlBgTheme = GlBgTheme.SPACE_DARK

    @Volatile
    var isPaused: Boolean = false

    @Volatile
    var isWireframe: Boolean = false

    @Volatile
    var speedMultiplier: Float = 1.0f

    @Volatile
    var reverseDirection: Boolean = false

    @Volatile
    var cameraPitchDeg: Float = 18f

    @Volatile
    var cameraYawDeg: Float = -24f

    @Volatile
    var cameraDistance: Float = -8.5f

    var onTelemetryListener: ((GlTelemetry) -> Unit)? = null

    private var angle = 0f
    private var lastFrameNanos = 0L
    private var fpsFrameCount = 0
    private var lastFpsTimestampMs = 0L
    private var currentFps = 0

    private var glVendor = "Unknown"
    private var glRenderer = "Unknown"
    private var glVersion = "OpenGL ES"
    private var viewportW = 0
    private var viewportH = 0

    fun rotateCameraBy(deltaYaw: Float, deltaPitch: Float) {
        cameraYawDeg = (cameraYawDeg + deltaYaw) % 360f
        cameraPitchDeg = (cameraPitchDeg + deltaPitch).coerceIn(-85f, 85f)
    }

    fun adjustZoomBy(scaleFactor: Float) {
        if (scaleFactor <= 0f) return
        val next = cameraDistance / scaleFactor
        cameraDistance = next.coerceIn(-16f, -4f)
    }

    fun resetCamera() {
        cameraPitchDeg = if (sceneMode == GlSceneMode.CUBE_3D) 18f else 0f
        cameraYawDeg = if (sceneMode == GlSceneMode.CUBE_3D) -24f else 0f
        cameraDistance = if (sceneMode == GlSceneMode.CUBE_3D) -8.5f else -10f
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        val theme = bgTheme
        gl.glClearColor(theme.r, theme.g, theme.b, 1.0f)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glClearDepthf(1.0f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glDepthFunc(GL10.GL_LEQUAL)
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST)

        glVendor = gl.glGetString(GL10.GL_VENDOR) ?: "Unknown"
        glRenderer = gl.glGetString(GL10.GL_RENDERER) ?: "Unknown"
        glVersion = gl.glGetString(GL10.GL_VERSION) ?: "OpenGL ES"

        lastFrameNanos = SystemClock.elapsedRealtimeNanos()
        lastFpsTimestampMs = SystemClock.elapsedRealtime()
        fpsFrameCount = 0
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        val safeHeight = height.coerceAtLeast(1)
        viewportW = width
        viewportH = safeHeight

        gl.glViewport(0, 0, width, safeHeight)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluPerspective(
            gl,
            45.0f,
            width.toFloat() / safeHeight.toFloat(),
            0.1f,
            100.0f
        )
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    override fun onDrawFrame(gl: GL10) {
        val nowNanos = SystemClock.elapsedRealtimeNanos()
        val deltaSec = if (lastFrameNanos == 0L) {
            0.016f
        } else {
            ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
        }
        lastFrameNanos = nowNanos

        if (!isPaused) {
            val dir = if (reverseDirection) -1f else 1f
            angle = (angle + 55f * speedMultiplier * dir * deltaSec) % 3600f
        }

        val theme = bgTheme
        gl.glClearColor(theme.r, theme.g, theme.b, 1.0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()

        // Apply interactive camera zoom + pitch/yaw
        gl.glTranslatef(0f, 0f, cameraDistance)
        gl.glRotatef(cameraPitchDeg, 1f, 0f, 0f)
        gl.glRotatef(cameraYawDeg, 0f, 1f, 0f)

        when (sceneMode) {
            GlSceneMode.ORBIT_SQUARES -> drawOrbitSquares(gl)
            GlSceneMode.CUBE_3D -> drawCubeScene(gl)
            GlSceneMode.FLAT_MATRIX -> drawFlatGeometryMatrix(gl)
        }

        updateFpsAndEmitTelemetry()
    }

    private fun drawOrbitSquares(gl: GL10) {
        // Center Square A
        gl.glPushMatrix()
        gl.glRotatef(angle, 0f, 0f, 1f)
        drawSquareOrWireframe(gl, smoothSquare, 0.01f, 0.85f, 0.77f)
        gl.glPopMatrix()

        // Orbiting Square B + Satellite Square C
        gl.glPushMatrix()
        gl.glRotatef(-angle, 0f, 0f, 1f)
        gl.glTranslatef(2.1f, 0f, 0f)
        gl.glScalef(0.5f, 0.5f, 0.5f)
        drawSquareOrWireframe(gl, smoothSquare, 0.25f, 0.70f, 0.98f)

        gl.glPushMatrix()
        gl.glRotatef(-angle * 1.6f, 0f, 0f, 1f)
        gl.glTranslatef(2.2f, 0f, 0f)
        gl.glScalef(0.5f, 0.5f, 0.5f)
        gl.glRotatef(angle * 4f, 0f, 0f, 1f)
        drawSquareOrWireframe(gl, smoothSquare, 1f, 0.45f, 0.55f)
        gl.glPopMatrix()

        gl.glPopMatrix()
    }

    private fun drawCubeScene(gl: GL10) {
        // Primary 3D Cube
        gl.glPushMatrix()
        gl.glRotatef(angle, 0.4f, 1f, 0.2f)
        colorCube.draw(gl, isWireframe)
        gl.glPopMatrix()

        // Orbiting Satellite 3D Cube
        gl.glPushMatrix()
        gl.glRotatef(-angle * 1.3f, 0f, 1f, 0.3f)
        gl.glTranslatef(2.6f, 0.4f, 0f)
        gl.glScalef(0.32f, 0.32f, 0.32f)
        gl.glRotatef(angle * 2.5f, 1f, 0.5f, 0f)
        colorCube.draw(gl, isWireframe)
        gl.glPopMatrix()
    }

    private fun drawFlatGeometryMatrix(gl: GL10) {
        val count = 6
        for (i in 0 until count) {
            val phase = i * (360f / count)
            gl.glPushMatrix()
            gl.glRotatef(angle + phase, 0f, 0f, 1f)
            gl.glTranslatef(1.85f, 0f, (i - 2.5f) * 0.12f)
            gl.glRotatef(angle * 1.5f, 0f, 1f, 0f)
            gl.glScalef(0.48f, 0.48f, 0.48f)
            drawSquareOrWireframe(gl, flatSquare, 0.5f, 0.7f, 1.0f)
            gl.glPopMatrix()
        }

        // Core center square
        gl.glPushMatrix()
        gl.glRotatef(-angle * 1.2f, 0f, 0f, 1f)
        gl.glScalef(0.65f, 0.65f, 0.65f)
        drawSquareOrWireframe(gl, smoothSquare, 0.01f, 0.85f, 0.77f)
        gl.glPopMatrix()
    }

    private fun drawSquareOrWireframe(gl: GL10, sq: Square, r: Float, g: Float, b: Float) {
        if (!isWireframe) {
            sq.draw(gl)
        } else {
            gl.glLineWidth(3.0f)
            gl.glColor4f(r, g, b, 1.0f)
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareOutlineBuffer)
            gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 4)
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        }
    }

    private fun updateFpsAndEmitTelemetry() {
        fpsFrameCount++
        val nowMs = SystemClock.elapsedRealtime()
        val elapsedMs = nowMs - lastFpsTimestampMs
        if (elapsedMs >= 450L) {
            currentFps = ((fpsFrameCount * 1000f) / elapsedMs).toInt().coerceAtLeast(0)
            fpsFrameCount = 0
            lastFpsTimestampMs = nowMs
            onTelemetryListener?.invoke(
                GlTelemetry(
                    fps = currentFps,
                    vendor = glVendor,
                    renderer = glRenderer,
                    version = glVersion,
                    viewportWidth = viewportW,
                    viewportHeight = viewportH,
                    pitchDeg = cameraPitchDeg,
                    yawDeg = cameraYawDeg,
                    zoomDistance = cameraDistance
                )
            )
        }
    }
}
