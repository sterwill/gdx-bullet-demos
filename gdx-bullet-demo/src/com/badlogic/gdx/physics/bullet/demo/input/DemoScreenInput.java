package com.badlogic.gdx.physics.bullet.demo.input;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.physics.bullet.demo.screens.DemoScreen;

public class DemoScreenInput extends InputAdapter
{
    private final Random random = new Random(0);
    private final DemoScreen screen;
    private final Input gdxInput;

    public DemoScreenInput(DemoScreen screen, Input gdxInput)
    {
        this.screen = screen;
        this.gdxInput = gdxInput;
    }

    public void poll(float graphicsDelta, float physicsDelta)
    {
    }

    @Override
    public boolean keyTyped(char character)
    {
        switch (character)
        {
        case 'p':
            if (screen.isPaused())
            {
                screen.resume();
            }
            else
            {
                screen.pause();
            }
            return true;
        case 'g':
            System.out.println("GC");
            System.gc();
            return true;
        case 'd':
            for (int i = 0; i < 20; i++)
            {
                screen.dropThing(random.nextBoolean());
            }
            return true;
        case 'r':
            screen.getGame().getScreen().dispose();
            screen.getGame().setScreen(new DemoScreen(screen.getGame()));
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button)
    {
        screen.dropThing((x < Gdx.graphics.getWidth() / 2));
        return true;
    }
}
