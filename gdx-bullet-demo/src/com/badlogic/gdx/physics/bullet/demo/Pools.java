package com.badlogic.gdx.physics.bullet.demo;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.utils.Pool;

public class Pools
{
    // Gdx types
    public static final Pool<Vector3> VECTOR3 = new Pool<Vector3>()
    {
        @Override
        protected Vector3 newObject()
        {
            return new Vector3();
        }
    };

    public static final Pool<Matrix3> MATRIX3 = new Pool<Matrix3>()
    {
        @Override
        protected Matrix3 newObject()
        {
            return new Matrix3();
        }
    };

    public static final Pool<Matrix4> MATRIX4 = new Pool<Matrix4>()
    {
        @Override
        protected Matrix4 newObject()
        {
            return new Matrix4();
        }
    };

    public static final Pool<BoundingBox> BOUNDINGBOX = new Pool<BoundingBox>()
    {
        @Override
        protected BoundingBox newObject()
        {
            return new BoundingBox();
        }
    };

    public static final Pool<Quaternion> QUATERNION = new Pool<Quaternion>()
    {
        @Override
        protected Quaternion newObject()
        {
            return new Quaternion().idt();
        }
    };

    // Bullet types
    public static final Pool<btTransform> btTRANSFORM = new Pool<btTransform>()
    {
        @Override
        protected btTransform newObject()
        {
            return new btTransform();
        }
    };
}
