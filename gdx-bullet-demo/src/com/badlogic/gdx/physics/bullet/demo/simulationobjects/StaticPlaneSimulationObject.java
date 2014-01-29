package com.badlogic.gdx.physics.bullet.demo.simulationobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btStaticPlaneShape;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.physics.bullet.demo.BulletDemoMath;
import com.badlogic.gdx.physics.bullet.demo.Pools;
import com.badlogic.gdx.physics.bullet.demo.screens.SimulationScreen;

/**
 * Really needs directional or ambient light because the vertices are at the edges (few normals).
 */
public class StaticPlaneSimulationObject extends RigidSimulationObject
{
    /**
     * The mesh's "up".
     */
    private final static Vector3 MESH_NORMAL = new Vector3(0, 0, 1);

    /**
     * Ignored. Only the plane normal and constants are used.
     */
    private final static btTransform DEFAULT_START_TRANSFORM = new btTransform();

    static
    {
        DEFAULT_START_TRANSFORM.setIdentity();
    }

    private final btStaticPlaneShape staticPlaneShape;
    private final Mesh mesh;
    private final Texture texture;
    private final boolean disposeTexture;

    public StaticPlaneSimulationObject(Vector3 planeNormal, float planeConstant, float friction, int width, int height,
            Texture texture, boolean disposeTexture)
    {
        super();

        this.texture = texture;
        this.disposeTexture = disposeTexture;

        this.mesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "position"), new VertexAttribute(
                Usage.TextureCoordinates, 2, "texture"));

        this.mesh.setVertices(new float[] {
                // Bottom left, tex
                -width / 2f, -height / 2f, 0, 0, 0,
                // Bottom right, tex
                width / 2f, -height / 2f, 0, width, 0,
                // Top right, tex
                width / 2f, height / 2f, 0, width, height,
                // Top left, tex
                -width / 2f, height / 2f, 0, 0, height });
        this.mesh.setIndices(new short[] { 0, 1, 2, 0, 3, 2 });

        this.staticPlaneShape = new btStaticPlaneShape(planeNormal, planeConstant);

        initialize(this.staticPlaneShape, 0, -1, DEFAULT_START_TRANSFORM);
    }

    @Override
    public void dispose()
    {
        super.dispose();

        staticPlaneShape.delete();

        mesh.dispose();

        if (disposeTexture)
        {
            texture.dispose();
        }
    }

    @Override
    public void render(SimulationScreen screen)
    {
        /*
         * Our StaticPlaneShape holds the plane vector and scalar. Use it instead of the applied render transform from
         * the kinematic (not updated/calculated for static planes).
         */

        Gdx.gl10.glPushMatrix();

        // Get the plane normal
        final Vector3 planeNormal = Pools.VECTOR3.obtain();
        planeNormal.set(staticPlaneShape.getPlaneNormal());

        final float planeConstant = staticPlaneShape.getPlaneConstant();

        // Calculate the plane origin
        final Vector3 planeOrigin = Pools.VECTOR3.obtain();
        planeOrigin.set(planeNormal);
        planeOrigin.scale(planeConstant, planeConstant, planeConstant);

        // Move to the origin
        Gdx.gl10.glTranslatef(planeOrigin.x, planeOrigin.y, planeOrigin.z);

        // Compute the angle difference between the plane and mesh normal ("up")
        float angle = MathUtils.radiansToDegrees * BulletDemoMath.angle(planeNormal, MESH_NORMAL);

        // Cross product gives us the vector around which to rotate
        planeNormal.crs(MESH_NORMAL);

        // Rotate by that angle
        Gdx.gl10.glRotatef(angle, planeNormal.x, planeNormal.y, planeNormal.z);

        Gdx.gl10.glColor4f(1, 1, 1, 1);
        Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
        Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);

        screen.enableLights();
        texture.bind();
        mesh.render(GL10.GL_TRIANGLE_STRIP);

        Gdx.gl10.glPopMatrix();

        Pools.VECTOR3.free(planeNormal);
        Pools.VECTOR3.free(planeOrigin);
    }
}
