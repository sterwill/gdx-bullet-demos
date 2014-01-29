package com.badlogic.gdx.physics.bullet.demo.screens;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btBoxShape;
import com.badlogic.gdx.physics.bullet.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.physics.bullet.btSphereShape;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.physics.bullet.btTriangleMesh;
import com.badlogic.gdx.physics.bullet.demo.Pools;
import com.badlogic.gdx.physics.bullet.demo.input.DemoScreenInput;
import com.badlogic.gdx.physics.bullet.demo.simulationobjects.MeshSimulationObject;
import com.badlogic.gdx.physics.bullet.demo.simulationobjects.RigidSimulationObject;

public class DemoScreen extends SimulationScreen
{
    private static final float[] SKY_COLOR = new float[] { .65f, .65f, 1, 1 };

    private final Game game;
    private final DemoScreenInput input;
    private final Random random = new Random();

    private Mesh cubeMesh;
    private Mesh icosphereMesh;
    private Mesh terrainMesh;

    private Texture cubeTexture;
    private Texture icosphereTexture;
    private Texture terrainTexture;

    private RigidSimulationObject terrain;
    // Holds a reference to Bullet's native mesh for the lifetime of the screen
    private AtomicReference<btTriangleMesh> terrainTriangleMesh = new AtomicReference<btTriangleMesh>();

    private final StringBuffer osdStringBuffer = new StringBuffer();

    public DemoScreen(Game game)
    {
        // Physics is configured when super() finishes
        super();

        this.game = game;
        this.input = new DemoScreenInput(this, Gdx.input);

        Gdx.app.getInput().setInputProcessor(input);

        // Ambient light
        Gdx.gl10.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, new float[] { .2f, .2f, .2f, 1 }, 0);

        // One directional light
        Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[] { .2f, .2f, .2f, 1 }, 0);
        Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[] { 1, 1, 1, 1 }, 0);
        Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, new float[] { 1, 1, 1, 1 }, 0);
        Gdx.gl10.glLightf(GL10.GL_LIGHT0, GL10.GL_CONSTANT_ATTENUATION, 1);
        Gdx.gl10.glLightf(GL10.GL_LIGHT0, GL10.GL_LINEAR_ATTENUATION, 0);
        Gdx.gl10.glLightf(GL10.GL_LIGHT0, GL10.GL_QUADRATIC_ATTENUATION, 0);
        Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] { -10, -10, 10, 1 }, 0);
        Gdx.gl10.glEnable(GL10.GL_LIGHT0);

        PerspectiveCamera camera = getPerspectiveCamera();
        camera.position.set(-25, 0, 20);
        camera.lookAt(0, 0, 0);
        camera.up.set(0, 0, 1);
        camera.fieldOfView = 60;
        camera.update();

        // Load meshes
        cubeMesh = ObjLoader.loadObj(Gdx.app.getFiles().classpath("models/cube.obj").read(), false);
        icosphereMesh = ObjLoader.loadObj(Gdx.app.getFiles().classpath("models/icosphere.obj").read(), false);
        terrainMesh = ObjLoader.loadObj(Gdx.app.getFiles().classpath("models/terrain.obj").read(), false);

        // Load textures
        cubeTexture = new Texture(Gdx.files.classpath("textures/weird.png"), true);
        cubeTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Nearest);
        icosphereTexture = new Texture(Gdx.files.classpath("textures/blue.png"), true);
        icosphereTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Nearest);
        terrainTexture = new Texture(Gdx.files.classpath("textures/grass.png"), true);
        terrainTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Nearest);
        terrainTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    }

    @Override
    public void dispose()
    {
        // Disposes all SimulationObjects we added
        super.dispose();

        cubeMesh.dispose();
        icosphereMesh.dispose();
        terrainMesh.dispose();

        cubeTexture.dispose();
        icosphereTexture.dispose();
        terrainTexture.dispose();

        // Only remove if we're still it
        if (Gdx.app.getInput().getInputProcessor() == input)
        {
            Gdx.app.getInput().setInputProcessor(null);
        }
    }

    public Game getGame()
    {
        return game;
    }

    public void dropThing(boolean type)
    {
        if (!isPaused())
        {
            float x = (random.nextFloat() * 10f) - 5f;
            float y = (random.nextFloat() * 10f) - 5f;
            float z = 20;

            final btTransform transform = Pools.btTRANSFORM.obtain();
            final Vector3 vector = Pools.VECTOR3.obtain();
            final Matrix3 basis = Pools.MATRIX3.obtain();

            transform.setIdentity();
            transform.setOrigin(vector.set(x, y, z));

            MeshSimulationObject object;
            if (type)
            {
                // Don't autodispose the mesh and texture
                object = new MeshSimulationObject(cubeMesh, GL10.GL_TRIANGLES, false, cubeTexture, false);
                object.initialize(new btBoxShape(vector.set(1, 1, 1)), 50, -1, transform);
            }
            else
            {
                // Don't autodispose the mesh and texture
                object = new MeshSimulationObject(icosphereMesh, GL10.GL_TRIANGLES, false, icosphereTexture, false);
                object.initialize(new btSphereShape(1), 50, -1, transform);
            }

            addCollisionSimulationObject(object);

            Pools.btTRANSFORM.free(transform);
            Pools.VECTOR3.free(vector);
            Pools.MATRIX3.free(basis);
        }
    }

    // Required by SimulationScreen

    @Override
    public void positionLights(float graphicsDelta, float physicsDelta)
    {
        // Our light doesn't move
    }

    @Override
    public void enableLights()
    {
        Gdx.gl10.glEnable(GL10.GL_LIGHTING);
    }

    @Override
    public void disableLights()
    {
        Gdx.gl10.glDisable(GL10.GL_LIGHTING);
    }

    @Override
    protected CharSequence getOSDText()
    {
        osdStringBuffer.setLength(0);
        osdStringBuffer.append("(d:drop 20, g:GC, p:pause, r:reset) ");
        osdStringBuffer.append(super.getOSDText());
        return osdStringBuffer;
    }

    // Hooks

    @Override
    protected void hookAddSimulationObjects()
    {
        final btTransform transform = Pools.btTRANSFORM.obtain();
        final Vector3 vector = Pools.VECTOR3.obtain();

        // Terrain
        transform.setIdentity();
        vector.set(0, 0, 0);
        transform.setOrigin(vector);
        terrain = new MeshSimulationObject(terrainMesh, GL10.GL_TRIANGLES, false, terrainTexture, false);
        terrain.initialize(MeshSimulationObject.createTriangleMeshShape(terrainMesh, terrainTriangleMesh), 0, -1,
                transform);
        terrain.getRigidbody().setCollisionFlags(CollisionFlags.CF_STATIC_OBJECT);
        addCollisionSimulationObject(terrain);

        // terrain = new StaticPlaneSimulationObject(vector.set(0, 0, 1), 1, -1, 50, 50, terrainTexture, false);
        // addCollisionSimulationObject(terrain);

        Pools.btTRANSFORM.free(transform);
        Pools.VECTOR3.free(vector);
    }

    @Override
    protected void hookRenderPostClear(float graphicsDelta, float physicsDelta)
    {
        Gdx.gl10.glClearColor(SKY_COLOR[0], SKY_COLOR[1], SKY_COLOR[2], SKY_COLOR[3]);

        enableFog();
    }

    @Override
    protected void hookRenderPrePhysics(float graphicsDelta)
    {
    }

    @Override
    protected void hookRenderPostPhysics(float graphicsDelta, float physicsDelta)
    {
        input.poll(graphicsDelta, physicsDelta);
    }

    @Override
    protected void hookRenderScene(float graphicsDelta, float physicsDelta)
    {
        // Render any scene objects that aren't simulation objects
    }

    @Override
    protected void hookRenderOSD(float graphicsDelta, float physicsDelta)
    {
        // Put stuff on the OSD
    }

    protected void enableFog()
    {
        // A very thin sky colored haze
        Gdx.gl10.glEnable(GL10.GL_FOG);
        Gdx.gl10.glFogf(GL10.GL_FOG_MODE, GL10.GL_LINEAR);
        Gdx.gl10.glFogfv(GL10.GL_FOG_COLOR, SKY_COLOR, 0);
        Gdx.gl10.glFogf(GL10.GL_FOG_DENSITY, .75f);
        Gdx.gl10.glHint(GL10.GL_FOG_HINT, GL10.GL_DONT_CARE);
        Gdx.gl10.glFogf(GL10.GL_FOG_START, 30);
        Gdx.gl10.glFogf(GL10.GL_FOG_END, 100);
    }

    protected void disableFog()
    {
        Gdx.gl10.glDisable(GL10.GL_FOG);
    }
}
