package com.badlogic.gdx.physics.bullet.demo;

import com.badlogic.gdx.math.Vector3;

public class BulletDemoMath
{
    public static float angle(Vector3 first, Vector3 second)
    {
        double vDot = first.dot(second) / (first.len() * second.len());

        if (vDot < -1.0)
        {
            vDot = -1.0;
        }

        if (vDot > 1.0)
        {
            vDot = 1.0;
        }
        return ((float) (Math.acos(vDot)));
    }
}
