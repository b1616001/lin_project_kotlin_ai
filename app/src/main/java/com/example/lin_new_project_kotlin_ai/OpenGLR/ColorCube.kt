package com.example.lin_new_project_kotlin_ai.OpenGLR

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10

class ColorCube {

    private val vertices = floatArrayOf(
        // Front face
        -1f, -1f,  1f,
         1f, -1f,  1f,
         1f,  1f,  1f,
        -1f,  1f,  1f,
        // Back face
        -1f, -1f, -1f,
        -1f,  1f, -1f,
         1f,  1f, -1f,
         1f, -1f, -1f,
        // Top face
        -1f,  1f, -1f,
        -1f,  1f,  1f,
         1f,  1f,  1f,
         1f,  1f, -1f,
        // Bottom face
        -1f, -1f, -1f,
         1f, -1f, -1f,
         1f, -1f,  1f,
        -1f, -1f,  1f,
        // Right face
         1f, -1f, -1f,
         1f,  1f, -1f,
         1f,  1f,  1f,
         1f, -1f,  1f,
        // Left face
        -1f, -1f, -1f,
        -1f, -1f,  1f,
        -1f,  1f,  1f,
        -1f,  1f, -1f
    )

    private val colors = floatArrayOf(
        // Front - Teal
        0.01f, 0.85f, 0.77f, 1f,  0.01f, 0.85f, 0.77f, 1f,  0.01f, 0.60f, 0.54f, 1f,  0.01f, 0.60f, 0.54f, 1f,
        // Back - Coral Red
        0.95f, 0.30f, 0.35f, 1f,  0.95f, 0.30f, 0.35f, 1f,  0.75f, 0.18f, 0.25f, 1f,  0.75f, 0.18f, 0.25f, 1f,
        // Top - Sky Blue
        0.22f, 0.68f, 0.98f, 1f,  0.22f, 0.68f, 0.98f, 1f,  0.12f, 0.45f, 0.88f, 1f,  0.12f, 0.45f, 0.88f, 1f,
        // Bottom - Amber
        1.00f, 0.72f, 0.18f, 1f,  1.00f, 0.72f, 0.18f, 1f,  0.90f, 0.50f, 0.08f, 1f,  0.90f, 0.50f, 0.08f, 1f,
        // Right - Violet
        0.65f, 0.38f, 0.96f, 1f,  0.65f, 0.38f, 0.96f, 1f,  0.45f, 0.22f, 0.82f, 1f,  0.45f, 0.22f, 0.82f, 1f,
        // Left - Emerald
        0.18f, 0.82f, 0.46f, 1f,  0.18f, 0.82f, 0.46f, 1f,  0.08f, 0.62f, 0.34f, 1f,  0.08f, 0.62f, 0.34f, 1f
    )

    private val triangleIndices = shortArrayOf(
        0, 1, 2, 0, 2, 3,       // Front
        4, 5, 6, 4, 6, 7,       // Back
        8, 9, 10, 8, 10, 11,    // Top
        12, 13, 14, 12, 14, 15, // Bottom
        16, 17, 18, 16, 18, 19, // Right
        20, 21, 22, 20, 22, 23  // Left
    )

    private val edgeIndices = shortArrayOf(
        0, 1, 1, 2, 2, 3, 3, 0, // Front edges
        4, 5, 5, 6, 6, 7, 7, 4, // Back edges
        0, 4, 3, 5, 2, 6, 1, 7  // Connecting edges
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }

    private val colorBuffer: FloatBuffer = ByteBuffer.allocateDirect(colors.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(colors)
            position(0)
        }

    private val triangleIndexBuffer: ShortBuffer = ByteBuffer.allocateDirect(triangleIndices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply {
            put(triangleIndices)
            position(0)
        }

    private val edgeIndexBuffer: ShortBuffer = ByteBuffer.allocateDirect(edgeIndices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply {
            put(edgeIndices)
            position(0)
        }

    fun draw(gl: GL10, wireframe: Boolean) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer)

        if (wireframe) {
            gl.glLineWidth(3.0f)
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer)
            gl.glDrawElements(GL10.GL_LINES, edgeIndices.size, GL10.GL_UNSIGNED_SHORT, edgeIndexBuffer)
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY)
        } else {
            gl.glFrontFace(GL10.GL_CCW)
            gl.glEnable(GL10.GL_CULL_FACE)
            gl.glCullFace(GL10.GL_BACK)
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer)
            gl.glDrawElements(GL10.GL_TRIANGLES, triangleIndices.size, GL10.GL_UNSIGNED_SHORT, triangleIndexBuffer)
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY)
            gl.glDisable(GL10.GL_CULL_FACE)
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
    }
}
