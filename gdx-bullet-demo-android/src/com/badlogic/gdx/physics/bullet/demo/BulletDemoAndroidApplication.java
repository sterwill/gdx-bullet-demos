package com.badlogic.gdx.physics.bullet.demo;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class BulletDemoAndroidApplication extends AndroidApplication
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initialize(new BulletDemoDesktopApplication(), false);
    }
}