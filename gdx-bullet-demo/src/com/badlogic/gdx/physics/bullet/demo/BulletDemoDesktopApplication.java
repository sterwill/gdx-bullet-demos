package com.badlogic.gdx.physics.bullet.demo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.bullet.demo.screens.DemoScreen;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class BulletDemoDesktopApplication extends Game
{
    public BulletDemoDesktopApplication()
    {
    }

    @Override
    public void create()
    {
        // Preload the natives so we can use them in static class initialization
        GdxNativesLoader.load();

        final Screen playScreen = new DemoScreen(this);
        setScreen(playScreen);
    }
}