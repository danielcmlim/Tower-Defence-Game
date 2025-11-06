package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Main implements ApplicationListener {

    private SpriteBatch batch;
    private Texture playerTex;

    private float px = 120, py = 64;   // position
    private float vx = 0,   vy = 0;    // velocity
    private boolean onGround = true;

    // tunables
    private static final float GROUND_Y   = 64f;
    private static final float MOVE_ACCEL = 900f;
    private static final float MOVE_MAX   = 220f;
    private static final float FRICTION   = 1400f;
    private static final float GRAVITY    = -900f;
    private static final float JUMP_VY    = 380f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Try load core/assets/player.png, else fallback to a white square
        try {
            playerTex = new Texture("player.png");
        } catch (Exception e) {
            Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
            pm.setColor(1, 1, 1, 1);
            pm.fillRectangle(0, 0, 32, 32);
            playerTex = new Texture(pm);
            pm.dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump  = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        if (left ^ right) {
            float dir = right ? 1f : -1f;
            vx += dir * MOVE_ACCEL * dt;
        } else {
            if (vx > 0) vx = Math.max(0, vx - FRICTION * dt);
            else if (vx < 0) vx = Math.min(0, vx + FRICTION * dt);
        }
        vx = MathUtils.clamp(vx, -MOVE_MAX, MOVE_MAX);

        if (jump && onGround) {
            vy = JUMP_VY;
            onGround = false;
        }

        vy += GRAVITY * dt;
        px += vx * dt;
        py += vy * dt;

        if (py <= GROUND_Y) {
            py = GROUND_Y;
            vy = 0;
            onGround = true;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(playerTex, px, py);
        batch.end();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (playerTex != null) playerTex.dispose();
    }
}
