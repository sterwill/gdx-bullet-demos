package com.badlogic.gdx.physics.bullet.demo.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.btCollisionObject;
import com.badlogic.gdx.physics.bullet.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.physics.bullet.demo.WindowedStats;
import com.badlogic.gdx.physics.bullet.demo.simulationobjects.CollisionSimulationObject;

/**
 * An abstract screen that does physics simulation with gdx-bullet.
 * 
 * @author sterwill
 */
public abstract class SimulationScreen implements Screen
{
    static
    {
        Bullet.init();
    }

    // Set to true after show() completes for the first time
    private boolean shownOnce;

    // Tracks whether this screen is paused
    private boolean paused;

    // Bullet physics
    private final btBroadphaseInterface broadphase;
    private final btCollisionDispatcher dispatcher;
    private final btDiscreteDynamicsWorld dynamicsWorld;
    private final btConstraintSolver solver;
    private final btDefaultCollisionConfiguration collisionConfiguration;

    // Bullet profiling
    private final WindowedStats stepSimulationTimes = new WindowedStats(30);
    private long stepSimulationLastAverage = 0;
    private long stepSimulationLastAverageMillis = System.currentTimeMillis();

    /*
     * Physics time (mostly computed in nanoseconds)
     * 
     * We have to pass a float to Bullet, so calculate the fixed Bullet step with float-level precision, then work out
     * the equivalent integer nanosecond step using floats (using doubles might give a different result).
     */
    protected static final float PHYSICS_TIME_STEP_SECONDS = 1f / 60f;
    protected static final long PHYSICS_TIME_STEP_NANOS = (long) (PHYSICS_TIME_STEP_SECONDS * 1000000000f);
    private long physicsCurrentTime;
    private long physicsAccumulator;

    // A list of all our scene objects
    private final List<CollisionSimulationObject> collisionSimulationObjects = new ArrayList<CollisionSimulationObject>();

    // OSD
    private final SpriteBatch osdSpriteBatch = new SpriteBatch();
    private final BitmapFont osdFont = new BitmapFont();
    private final StringBuilder osdStringBuilder = new StringBuilder(1024);

    // Perspective camera
    private final PerspectiveCamera perspectiveCamera = new PerspectiveCamera();

    // Orthographic OSD camera
    private final OrthographicCamera osdCamera = new OrthographicCamera();

    // Preallocated for use when rendering the physics objects
    private final btTransform transform = new btTransform();
    private final float[] glMatrix = new float[16];

    public SimulationScreen()
    {
        /*
         * Allocate and initialize Bullet.
         * 
         * Things to know when using Bullet with libgdx:
         * 
         * Call Bullet.init() to load the native library before using any other Bullet types. This class does it in a
         * static initializer because it has Bullet types in fields.
         * 
         * When you're done with a Bullet object (class names start with "bt"), dispose of the native object by calling
         * .delete(). Remember to remove your collision objects from the world before deleting them.
         * 
         * It's important to prevent Java from garbage-collecting Bullet objects while they're in use. If you don't, the
         * JVM will almost certainly crash when you step your simulation (possibly seconds or minutes later). For this
         * class, objects will be in use as long as this screen is valid, so we store them in fields and delete them in
         * the dispose method. You could also store them in collections or use some other method to keep them around.
         * 
         * You'll want to read the Bullet API docs to know which methods take pointers vs. references to other objects.
         * Usually when a reference is taken, the parameter object's data is _copied_ into the target and you're free to
         * do whatever you want with the parameter object after the method returns. You don't have to keep the Java
         * object from getting GCed in this case.
         * 
         * If instead Bullet takes a pointer to an object, it probably needs it to be kept around (including the Java
         * object) until you unhook it or assign some other object in its place.
         * 
         * Some Bullet methods return libgdx types (Vector3, Matrix4, Quaternion) for convenience. These returned
         * objects are re-used by all Bullet methods that return those types, to avoid allocating new Java objects. You
         * should copy the data out of these objects before calling another Bullet method that returns one of these
         * types, or it will be overwritten during the next call.
         * 
         * libgdx types (Vector3, Matrix4, Quaternion) that are _inputs_ to Bullet methods are not used in any special
         * way. They are treated as value types (the data is always copied into Bullet) and no special access rules
         * apply.
         */

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();

        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3(0f, 0f, -9.8f));

        physicsCurrentTime = System.nanoTime();
        physicsAccumulator = 0;
    }

    public btDiscreteDynamicsWorld getDynamicsWorld()
    {
        return dynamicsWorld;
    }

    public PerspectiveCamera getPerspectiveCamera()
    {
        return perspectiveCamera;
    }

    public OrthographicCamera getOSDCamera()
    {
        return osdCamera;
    }

    @Override
    public void show()
    {
        if (!shownOnce)
        {
            shownOnce = true;

            // Resize does some more work on cameras, including update()
            resize(Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());

            // Add scene objects including player
            hookAddSimulationObjects();
        }
    }

    @Override
    public void hide()
    {
    }

    @Override
    public void pause()
    {
        paused = true;
    }

    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public void resume()
    {
        paused = false;
    }

    // Required virtuals

    /**
     * Called each frame after the camera is positioned (projection and modelview matrixes set), but before any scene
     * objects have been rendered, to position any positionable lights. Directional lights may be set once in some other
     * hook and not updated by this method.
     */
    protected abstract void positionLights(float graphicsDelta, float physicsDelta);

    /**
     * Called at various times during {@link #render(float)} (but always after {@link #positionLights(float, float)} has
     * been called for that frame) to enable lighting.
     */
    public abstract void enableLights();

    /**
     * Called at various times during {@link #render(float)} (but always after {@link #positionLights(float, float)} has
     * been called for that frame) to disable lighting.
     */
    public abstract void disableLights();

    /**
     * Called by {@link #show()} the first time it executes so the derived class can add scene objects.
     */
    protected abstract void hookAddSimulationObjects();

    // Optional hooks

    /**
     * Called by {@link #render(float)} before the physics has been stepped for this frame.
     * 
     * @param graphicsDelta
     *            GDX's delta since last frame (may be smoothed, probably never 0)
     */
    protected void hookRenderPrePhysics(float graphicsDelta)
    {
    }

    /**
     * Called by {@link #render(float)} after the physics has been calculated. physicsDelta is the time the physics
     * engine was stepped this frame, which is useful for synchronizing things exactly with the physics engine.
     * 
     * @param graphicsDelta
     *            GDX's delta since last frame (may be smoothed, probably never 0)
     * @param physicsDelta
     *            the exact amount of time the physics world was just stepped (may be 0 when frame rate is much higher
     *            than fixed physics rate)
     */
    protected void hookRenderPostPhysics(float graphicsDelta, float physicsDelta)
    {
    }

    /**
     * Called by {@link #render(float)} after the screen has been cleared. This is a good time to turn on fixed function
     * pipeline options like fog.
     * <p>
     * Time delta values are exactly the same as passed to {@link #hookRenderPostPhysics(float, float)} for the same
     * frame.
     */
    protected void hookRenderPostClear(float graphicsDelta, float physicsDelta)
    {
    }

    /**
     * Called by {@link #render(float)} with OpenGL in a perspective projection, after simulation objects have been
     * rendered.
     * <p>
     * Time delta values are exactly the same as passed to {@link #hookRenderPostPhysics(float, float)} for the same
     * frame.
     */
    protected void hookRenderScene(float graphicsDelta, float physicsDelta)
    {
    }

    /**
     * Called by {@link #render(float)} with OpenGL in an orthographic projection, after the on screen display text has
     * been rendered, which is always done after {@link #hookRenderScene(float, float)} has completed.
     * <p>
     * Time delta values are exactly the same as passed to {@link #hookRenderPostPhysics(float, float)} for the same
     * frame.
     */
    protected void hookRenderOSD(float graphicsDelta, float physicsDelta)
    {
    }

    // Misc

    protected CharSequence getOSDText()
    {
        osdStringBuilder.setLength(0);

        if (paused)
        {
            osdStringBuilder.append("[PAUSED] ");
        }

        osdStringBuilder.append("fps: ");
        osdStringBuilder.append(Gdx.graphics.getFramesPerSecond());
        osdStringBuilder.append(" objects: ");
        osdStringBuilder.append(collisionSimulationObjects.size());

        long now = System.currentTimeMillis();
        if (now > stepSimulationLastAverageMillis + 1000)
        {
            stepSimulationLastAverage = stepSimulationTimes.average();
            stepSimulationLastAverageMillis = now;
        }

        osdStringBuilder.append(" stepSimulation: ");
        osdStringBuilder.append(stepSimulationLastAverage);
        osdStringBuilder.append(" ");

        return osdStringBuilder;
    }

    /**
     * Adds a {@link CollisionSimulationObject} to the dynamics world. All objects added to the dynamics world that are
     * still there when {@link #dispose()} is called will be disposed.
     * 
     * @param object
     *            the object to add
     */
    protected void addCollisionSimulationObject(CollisionSimulationObject object)
    {
        object.addToDynamicsWorld(dynamicsWorld);
        collisionSimulationObjects.add(object);
    }

    /**
     * Removes a {@link CollisionSimulationObject} from the dynamics world.
     * 
     * @param object
     *            the object to remove
     */
    protected void removeCollisionSimulationObject(CollisionSimulationObject object)
    {
        object.removeFromDynamicsWorld(dynamicsWorld);
        collisionSimulationObjects.remove(object);
    }

    @Override
    public final void render(float graphicsDelta)
    {
        // Physics
        hookRenderPrePhysics(graphicsDelta);
        final float physicsDelta = stepPhysics();
        hookRenderPostPhysics(graphicsDelta, physicsDelta);

        // Clear frame and enable model styles
        Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        hookRenderPostClear(graphicsDelta, physicsDelta);

        // Apply perspective player camera
        perspectiveCamera.apply(Gdx.gl10);

        Gdx.gl10.glEnable(GL10.GL_DITHER);
        Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
        Gdx.gl10.glEnable(GL10.GL_CULL_FACE);
        Gdx.gl10.glEnable(GL10.GL_BLEND);
        Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        // Position lights so lighting calculations for objects are correct
        positionLights(graphicsDelta, physicsDelta);

        renderDynamicsWorld();

        hookRenderScene(graphicsDelta, physicsDelta);

        // Apply orthographic OSD camera
        osdCamera.apply(Gdx.gl10);

        // Disable face culling so we draw everything
        Gdx.gl10.glDisable(GL10.GL_CULL_FACE);

        // Render OSD
        disableLights();
        osdSpriteBatch.begin();
        osdFont.setColor(1, 1, 1, 1f);
        osdFont.draw(osdSpriteBatch, getOSDText(), 10, 10 + osdFont.getCapHeight());
        osdSpriteBatch.end();
        hookRenderOSD(graphicsDelta, physicsDelta);
    }

    /**
     * Steps the physics world (or possibly doesn't if the frame rate is super awesomely fast and it's not needed yet).
     * 
     * @return the amount of time the physics world was stepped (may be 0)
     */
    private float stepPhysics()
    {
        /*
         * Fixed step physics. Basic premise is we always step by exactly PHYSICS_TIME_STEP, sometimes stepping more
         * than once to catch up if we were behind, and if there's not enough time yet for another simulation, leave the
         * remainder in the accumulator for next time.
         * 
         * Keeps our physics updating regularly even through terribly slow and blazingly fast framerates.
         * 
         * http://gafferongames.com/game-physics/fix-your-timestep/ (specifically "Free the physics")
         */
        long newTime = System.nanoTime();
        long frameTime = newTime - physicsCurrentTime;
        physicsCurrentTime = newTime;
        physicsAccumulator += frameTime;

        float physicsDelta = 0;
        while (physicsAccumulator >= PHYSICS_TIME_STEP_NANOS)
        {
            if (!paused)
            {
                /*
                 * Pass maxSubSteps = 0 for exactly one integration over the time specified by the third parameter.
                 * Bullet documentation warns against this, but we're doing our own make-up logic.
                 */
                long start = System.nanoTime();
                dynamicsWorld.stepSimulation(PHYSICS_TIME_STEP_SECONDS, 0, PHYSICS_TIME_STEP_SECONDS);
                long elapsed = System.nanoTime() - start;
                stepSimulationTimes.add(elapsed);
                physicsDelta += PHYSICS_TIME_STEP_SECONDS;
            }

            physicsAccumulator -= PHYSICS_TIME_STEP_NANOS;
        }

        return physicsDelta;
    }

    @Override
    public void resize(int width, int height)
    {
        perspectiveCamera.viewportWidth = width;
        perspectiveCamera.viewportHeight = height;
        perspectiveCamera.update();

        osdCamera.viewportWidth = width;
        osdCamera.viewportHeight = height;
        osdCamera.position.set(osdCamera.viewportWidth / 2, osdCamera.viewportHeight / 2, 0);
        osdCamera.update();
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@link SimulationScreen} will dispose any {@link CollisionSimulationObject}s that it currently manages (were
     * added with {@link #addCollisionSimulationObject(CollisionSimulationObject)} and were not removed with
     * {@link #removeCollisionSimulationObject(CollisionSimulationObject)} ).
     */
    @Override
    public void dispose()
    {
        // Remove all the objects from the world, then delete them.
        for (CollisionSimulationObject object : collisionSimulationObjects)
        {
            object.removeFromDynamicsWorld(dynamicsWorld);
            object.dispose();
        }

        collisionSimulationObjects.clear();

        // Delete the native bullet objects
        dynamicsWorld.delete();
        broadphase.delete();
        dispatcher.delete();
        solver.delete();
        collisionConfiguration.delete();
    }

    private void renderDynamicsWorld()
    {
        for (int i = 0; i < collisionSimulationObjects.size(); i++)
        {
            final CollisionSimulationObject simulationObject = collisionSimulationObjects.get(i);
            final btCollisionObject collisionObject = simulationObject.getCollisionObject();

            /*
             * Prefer the (interpolated) transform of the motion state.
             * 
             * Bullet offers native upcast methods, but the wrappers allocate new Java objects to hold the upcasted
             * object result, so simply cast to the types we know we use in our scene to avoid the allocation.
             */
            boolean handled = false;
            if (collisionObject instanceof btRigidBody)
            {
                final btDefaultMotionState ms = (btDefaultMotionState) ((btRigidBody) collisionObject).getMotionState();
                if (ms != null)
                {
                    ms.getGraphicsWorldTrans(transform);
                    handled = true;
                }
            }

            // Fall back to the world transform
            if (!handled)
            {
                collisionObject.getWorldTransform(transform);
            }

            Gdx.gl10.glPushMatrix();

            // Apply the object's transform to the OpenGL world
            transform.getOpenGLMatrix(glMatrix);
            Gdx.gl10.glMultMatrixf(glMatrix, 0);

            simulationObject.render(this);

            Gdx.gl10.glPopMatrix();
        }
    }
}
