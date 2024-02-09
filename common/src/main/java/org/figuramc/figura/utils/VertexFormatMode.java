package org.figuramc.figura.utils;

import org.lwjgl.opengl.GL11;

//Borrowed mostly from Minecraft
public enum VertexFormatMode {
    LINES(GL11.GL_TRIANGLES, 2, 2),
    LINE_STRIP(GL11.GL_TRIANGLE_STRIP, 2, 1),
    DEBUG_LINES(GL11.GL_LINES, 2, 2),
    DEBUG_LINE_STRIP(GL11.GL_LINE_STRIP, 2, 1),
    TRIANGLES(GL11.GL_TRIANGLES, 3, 3),
    TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP, 3, 1),
    TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN, 3, 1),
    QUADS(GL11.GL_QUADS, 4, 4); // In modern GL, GL_QUADS doesn't exist anymore, so GL_TRIANGLES is used instead, need to check if using it works correctly!

    public final int asGLMode;
    public final int primitiveLength;
    public final int primitiveStride;

    private VertexFormatMode(int mode, int vertexCount, int size) {
        this.asGLMode = mode;
        this.primitiveLength = vertexCount;
        this.primitiveStride = size;
    }

    public int indexCount(int vertexCount) {
        switch (this) {
            case LINE_STRIP:
            case DEBUG_LINES:
            case DEBUG_LINE_STRIP:
            case TRIANGLES:
            case TRIANGLE_STRIP:
            case TRIANGLE_FAN:
                return vertexCount;
            case LINES:
            case QUADS:
                return vertexCount / 4 * 6;
            default:
                return 0;
        }
    }
}
