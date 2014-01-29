package com.badlogic.gdx.physics.bullet.demo.simulationobjects;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.btTriangleMesh;
import com.badlogic.gdx.physics.bullet.demo.Pools;
import com.badlogic.gdx.physics.bullet.demo.screens.SimulationScreen;

public class MeshSimulationObject extends RigidSimulationObject
{
    protected final Mesh mesh;
    protected final int meshPrimitiveType;
    private final boolean disposeMesh;
    protected final Texture texture;
    private final boolean disposeTexture;

    public MeshSimulationObject(Mesh mesh, int meshPrimitiveType, boolean disposeMesh, Texture texture,
            boolean disposeTexture)
    {
        super();

        this.mesh = mesh;
        this.meshPrimitiveType = meshPrimitiveType;
        this.disposeMesh = disposeMesh;
        this.texture = texture;
        this.disposeTexture = disposeTexture;
    }

    @Override
    public void dispose()
    {
        super.dispose();

        if (disposeMesh)
        {
            mesh.dispose();
        }

        if (disposeTexture)
        {
            texture.dispose();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Renders with textures disabled and color material enabled. Override to change this (bind textures, etc.).
     */
    @Override
    public void render(SimulationScreen screen)
    {
        Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);
        Gdx.gl10.glColor4f(1, 1, 1, 1);

        if (texture != null)
        {
            Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
            texture.bind();
        }
        else
        {
            Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
        }

        screen.enableLights();

        mesh.render(meshPrimitiveType);
    }

    public static btBvhTriangleMeshShape createTriangleMeshShape(Mesh mesh, AtomicReference<btTriangleMesh> triangleMesh)
    {
        final btTriangleMesh m = new btTriangleMesh();

        final ShortBuffer indices = mesh.getIndicesBuffer();
        indices.rewind();
        final FloatBuffer vertices = mesh.getVerticesBuffer();
        vertices.rewind();

        // Some meshes have vertices but no indices declared
        final boolean hasIndices = mesh.getNumIndices() != 0;
        final int vertexStride = mesh.getVertexSize() / 4;

        final Vector3 v0 = Pools.VECTOR3.obtain();
        final Vector3 v1 = Pools.VECTOR3.obtain();
        final Vector3 v2 = Pools.VECTOR3.obtain();

        // Set up a small array to keep the loop code simpler
        final Vector3[] vectors = new Vector3[] { v0, v1, v2 };
        short vectorIndex = 0;

        int i = -1;
        int verticesRead = 0;
        while (verticesRead < mesh.getNumVertices())
        {
            if (hasIndices)
            {
                i = indices.get();
            }
            else
            {
                i++;
            }

            vectors[vectorIndex++].set(vertices.get(i * vertexStride), vertices.get(i * vertexStride + 1),
                    vertices.get(i * vertexStride + 2));

            if (vectorIndex == vectors.length)
            {
                m.addTriangle(v0, v1, v2, true);
                vectorIndex = 0;
            }

            verticesRead++;
        }

        Pools.VECTOR3.free(v0);
        Pools.VECTOR3.free(v1);
        Pools.VECTOR3.free(v2);

        triangleMesh.set(m);
        return new btBvhTriangleMeshShape(m, true);
    }
}
