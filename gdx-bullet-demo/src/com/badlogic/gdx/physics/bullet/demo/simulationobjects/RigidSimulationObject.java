package com.badlogic.gdx.physics.bullet.demo.simulationobjects;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btMotionState;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.physics.bullet.demo.Pools;
import com.badlogic.gdx.physics.bullet.demo.screens.SimulationScreen;

/**
 * A {@link CollisionSimulationObject} associated with a {@link RigidBody} type of {@link CollisionObject}.
 * <p>
 * Call {@link #initialize(btCollisionShape, float, float, btTransform)} to create the {@link btRigidBody} (accessible
 * with {@link #getRigidbody()} and {@link #getCollisionObject()}).
 * 
 * @author sterwill
 */
public abstract class RigidSimulationObject extends CollisionSimulationObject
{
    private btCollisionShape collisionShape;
    private btRigidBody rigidBody;

    /**
     * This motion state is captured by the btRigidBody and must be kept around until the btRigidBody is disposed.
     */
    private btMotionState motionState;

    public RigidSimulationObject()
    {
        super();
    }

    @Override
    public void dispose()
    {
        super.dispose();

        if (rigidBody != null)
        {
            rigidBody.delete();
        }

        if (collisionShape != null)
        {
            collisionShape.delete();
        }

        if (motionState != null)
        {
            motionState.delete();
        }
    }

    /**
     * Initializes the {@link RigidSimulationObject} from the given shape and other parameters. Call this once before
     * adding to the dynamics world.
     * <p>
     * 
     * @param collisionShape
     *            the shape (the reference is held by this class and will be diposed automatically)
     * @param mass
     *            the mass
     * @param friction
     *            the friction or -1 for default
     * @param startTransform
     *            the start transform (reference is not captured)
     */
    public void initialize(btCollisionShape collisionShape, float mass, float friction, btTransform startTransform)
    {
        this.collisionShape = collisionShape;

        motionState = new btDefaultMotionState(startTransform);

        final Vector3 localInertia = Pools.VECTOR3.obtain();
        localInertia.set(0, 0, 0);

        if (mass != 0)
        {
            collisionShape.calculateLocalInertia(mass, localInertia);
        }

        final btRigidBodyConstructionInfo bodyCI = new btRigidBodyConstructionInfo(mass, motionState, collisionShape,
                localInertia);

        if (friction != -1)
        {
            bodyCI.setM_friction(friction);
        }

        rigidBody = new btRigidBody(bodyCI);

        // All fields copied during construction
        bodyCI.delete();

        setCollisionObject(rigidBody);

        Pools.VECTOR3.free(localInertia);
    }

    @Override
    public void render(SimulationScreen screen)
    {
    }

    public btRigidBody getRigidbody()
    {
        return rigidBody;
    }

    @Override
    public void addToDynamicsWorld(btDiscreteDynamicsWorld dynamicsWorld)
    {
        // Use the rigid method
        dynamicsWorld.addRigidBody(getRigidbody(), getCollisionFilterGroup(), getCollisionFilterMask());
    }

    @Override
    public void removeFromDynamicsWorld(btDiscreteDynamicsWorld dynamicsWorld)
    {
        // Use the rigid method
        dynamicsWorld.removeRigidBody(getRigidbody());
    }
}
