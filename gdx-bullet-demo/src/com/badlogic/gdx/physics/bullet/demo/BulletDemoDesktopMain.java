package com.badlogic.gdx.physics.bullet.demo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class BulletDemoDesktopMain
{
    public static void main(String[] argv)
    {
        BulletDemoDesktopApplication app = new BulletDemoDesktopApplication();

        new LwjglApplication(app, "Bullet Demo", 800, 480, false);
    }
}