package com.badlogic.drop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class ParticleSystem {

    private final Array<Particle> particles = new Array<>();
    private final Texture coinTex;

    public ParticleSystem() {
        // Small golden circle
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 0.85f, 0.2f, 1f);
        pm.fillCircle(4, 4, 3);
        pm.setColor(1f, 0.95f, 0.4f, 1f);
        pm.fillCircle(4, 4, 2);
        coinTex = new Texture(pm);
        pm.dispose();
    }

    public void spawnCoinBurst(float x, float y, int coinCount) {
        for (int i = 0; i < coinCount; i++) {
            Particle p = new Particle();
            p.x       = x;
            p.y       = y;

            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(80f, 150f);

            p.vx      = MathUtils.cosDeg(angle) * speed;
            p.vy      = MathUtils.sinDeg(angle) * speed + 100f;
            p.life    = 1.2f;
            p.maxLife = 1.2f;
            p.gravity = -400f;

            particles.add(p);
        }
    }

    public void update(float dt) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);

            p.life -= dt;
            if (p.life <= 0f) { particles.removeIndex(i); continue; }

            p.vy += p.gravity * dt;
            p.x  += p.vx * dt;
            p.y  += p.vy * dt;
            p.vx *= 0.95f;
        }
    }

    public void draw(SpriteBatch batch) {
        for (Particle p : particles) {
            float alpha = MathUtils.clamp(p.life / p.maxLife, 0f, 1f);
            float scale = 1f + MathUtils.sin(p.life * 15f) * 0.2f;

            Color old = new Color(batch.getColor());
            batch.setColor(1f, 1f, 1f, alpha);

            float size = 8f * scale;
            batch.draw(coinTex, p.x - size / 2, p.y - size / 2, size, size);

            batch.setColor(old);
        }
    }

    public void dispose() {
        if (coinTex != null) coinTex.dispose();
    }

    static class Particle {
        float x, y;
        float vx, vy;
        float life, maxLife;
        float gravity;
    }
}
