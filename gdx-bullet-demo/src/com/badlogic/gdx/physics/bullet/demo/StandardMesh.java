package com.badlogic.gdx.physics.bullet.demo;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public abstract class StandardMesh
{

    // GL_LINES

    /**
     * 0,0,0 to 1,0,0
     */
    public static final Mesh UNIT_LINE_X;

    /**
     * 0,0,0 to 0,1,0
     */
    public static final Mesh UNIT_LINE_Y;

    /**
     * 0,0,0 to 0,0,1
     */
    public static final Mesh UNIT_LINE_Z;

    /**
     * Wireframe square with center at 0,0,0
     */
    public static final Mesh UNIT_LINE_SQUARE_XY;

    // GL_TRIANGLE_STRIP

    /**
     * Triangulated quad with center at 0,0,0. Vertex normals at 0,0,1.
     */
    public static final Mesh UNIT_QUAD_XY;

    static
    {
        // GL_LINES

        UNIT_LINE_X = new Mesh(true, 2, 2, new VertexAttribute(Usage.Position, 3, "position"));
        UNIT_LINE_X.setVertices(new float[] { 0, 0, 0, 1, 0, 0 });
        UNIT_LINE_X.setIndices(identityArray(2));

        UNIT_LINE_Y = new Mesh(true, 2, 2, new VertexAttribute(Usage.Position, 3, "position"));
        UNIT_LINE_Y.setVertices(new float[] { 0, 0, 0, 0, 1, 0 });
        UNIT_LINE_Y.setIndices(identityArray(2));

        UNIT_LINE_Z = new Mesh(true, 2, 2, new VertexAttribute(Usage.Position, 3, "position"));
        UNIT_LINE_Z.setVertices(new float[] { 0, 0, 0, 0, 0, 1 });
        UNIT_LINE_Z.setIndices(identityArray(2));

        UNIT_LINE_SQUARE_XY = new Mesh(true, 4, 24, new VertexAttribute(Usage.Position, 3, "position"));
        UNIT_LINE_SQUARE_XY.setVertices(new float[] {
                // 0 Near bottom left
                -.5f, -.5f, 0,
                // 1 Near bottom right
                .5f, -.5f, 0,
                // 2 Near top right
                .5f, .5f, 0,
                // 3 Near top left
                -.5f, .5f, 0 });
        UNIT_LINE_SQUARE_XY.setIndices(new short[] { 0, 1, 1, 2, 2, 3, 3, 0 });

        // GL_TRIANGLE_STRIP

        UNIT_QUAD_XY = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "position"), new VertexAttribute(
                Usage.TextureCoordinates, 2, "texture"));
        UNIT_QUAD_XY.setVertices(new float[] {
                // Bottom left, tex
                -.5f, -.5f, 0, 0, 0,
                // Bottom right, tex
                .5f, -.5f, 0, 1, 0,
                // Top right, tex
                .5f, .5f, 0, 1, 1,
                // Top left,tex
                -.5f, .5f, 0, 0, 1 });
        UNIT_QUAD_XY.setIndices(new short[] { 0, 1, 2, 0, 3, 2 });
    }

    public static short[] identityArray(int size)
    {
        final short[] a = new short[size];

        for (short i = 0; i < size; i++)
        {
            a[i] = i;
        }

        return a;
    }
}
