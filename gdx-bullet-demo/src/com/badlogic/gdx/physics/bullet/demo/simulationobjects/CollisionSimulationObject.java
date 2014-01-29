package com.badlogic.gdx.physics.bullet.demo.simulationobjects;

import com.badlogic.gdx.physics.bullet.btActionInterface;
import com.badlogic.gdx.physics.bullet.btBroadphaseProxy.CollisionFilterGroups;
import com.badlogic.gdx.physics.bullet.demo.screens.SimulationScreen;
import com.badlogic.gdx.physics.bullet.btCollisionObject;
import com.badlogic.gdx.physics.bullet.btDiscreteDynamicsWorld;
import com.badlogic.gdx.utils.Disposable;

/**
 * Associates a {@link btCollisionObject} from the dynamics world with something that can render itself, and possibly
 * exposes other behavior.
 * 
 * @author sterwill
 */
public abstract class CollisionSimulationObject implements Disposable
{
    protected btCollisionObject collisionObject;

    private short collisionFilterGroup;
    private boolean collisionFilterGroupSet;

    private short collisionFilterMask;
    private boolean collisionFilterMaskSet;

    protected btActionInterface actionInterface;

    public CollisionSimulationObject()
    {
    }

    @Override
    public void dispose()
    {
    }

    public void setCollisionObject(btCollisionObject collisionObject)
    {
        this.collisionObject = collisionObject;
    }

    public btCollisionObject getCollisionObject()
    {
        return collisionObject;
    }

    public void setCollisionFilterGroup(short collisionFilterGroup)
    {
        this.collisionFilterGroup = collisionFilterGroup;
        this.collisionFilterGroupSet = true;
    }

    public short getCollisionFilterGroup()
    {
        if (collisionFilterGroupSet)
        {
            return collisionFilterGroup;
        }

        // Similar to parameter defaults in native Blender
        return isDynamic() ? (short) CollisionFilterGroups.DefaultFilter : (short) CollisionFilterGroups.StaticFilter;
    }

    public void setCollisionFilterMask(short collisionFilterMask)
    {
        this.collisionFilterMask = collisionFilterMask;
        this.collisionFilterMaskSet = true;
    }

    public short getCollisionFilterMask()
    {
        if (collisionFilterMaskSet)
        {
            return collisionFilterMask;
        }

        // Similar to parameter defaults in native Blender
        return isDynamic() ? (short) CollisionFilterGroups.AllFilter
                : (short) (CollisionFilterGroups.AllFilter ^ CollisionFilterGroups.StaticFilter);
    }

    public void addToDynamicsWorld(btDiscreteDynamicsWorld dynamicsWorld)
    {
        if (collisionObject != null)
        {
            dynamicsWorld.addCollisionObject(collisionObject, getCollisionFilterGroup(), getCollisionFilterMask());
            if (actionInterface != null)
            {
                dynamicsWorld.addAction(actionInterface);
            }
        }
    }

    public void removeFromDynamicsWorld(btDiscreteDynamicsWorld dynamicsWorld)
    {
        if (collisionObject != null)
        {
            dynamicsWorld.removeCollisionObject(collisionObject);
            if (actionInterface != null)
            {
                dynamicsWorld.removeAction(actionInterface);
            }
        }
    }

    public abstract void render(SimulationScreen screen);

    /**
     * Sets an action interface that will be hooked by {@link #addToDynamicsWorld(DiscreteDynamicsWorld)} and unhooked
     * by {@link #removeFromDynamicsWorld(DiscreteDynamicsWorld)}. Most {@link btCollisionObject}s don't need to hook up
     * an {@link btActionInterface}.
     */
    protected void setActionInterface(btActionInterface btActionInterface)
    {
        this.actionInterface = btActionInterface;
    }

    protected btActionInterface getActionInterface()
    {
        return actionInterface;
    }

    private boolean isDynamic()
    {
        if (collisionObject == null)
        {
            return false;
        }

        return !(collisionObject.isStaticObject() || collisionObject.isKinematicObject());
    }
}
