package com.badlogic.drop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class ParticleSystem {

    private Array<Particle> particles = new Array<>();
    private Texture coinTex;

    public ParticleSystem() {
        // Create coin texture (small golden circle)
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 0.85f, 0.2f, 1f);
        pm.fillCircle(4, 4, 3);
        pm.setColor(1f, 0.95f, 0.4f, 1f);
        pm.fillCircle(4, 4, 2);
        coinTex = new Texture(pm);
        pm.dispose();
    }

    public void spawnCoinBurst(float x, float y, int coinCount) {
        // Create a burst of coin particles
        for (int i = 0; i < coinCount; i++) {
            Particle p = new Particle();
            p.x = x;
            p.y = y;

            // Random angle for spread
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(80f, 150f);

            p.vx = MathUtils.cosDeg(angle) * speed;
            p.vy = MathUtils.sinDeg(angle) * speed + 100f; // Add upward bias

            p.life = 1.2f;
            p.maxLife = 1.2f;
            p.gravity = -400f;

            particles.add(p);
        }
    }

    public void update(float dt) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);

            p.life -= dt;
            if (p.life <= 0f) {
                particles.removeIndex(i);
                continue;
            }

            // Apply velocity and gravity
            p.vy += p.gravity * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;

            // Slow down horizontal movement
            p.vx *= 0.95f;
        }
    }

    public void draw(SpriteBatch batch) {
        for (Particle p : particles) {
            // Fade out based on remaining life
            float alpha = MathUtils.clamp(p.life / p.maxLife, 0f, 1f);

            // Pulsing scale effect
            float scale = 1f + MathUtils.sin(p.life * 15f) * 0.2f;

            Color oldColor = batch.getColor();
            batch.setColor(1f, 1f, 1f, alpha);

            float size = 8f * scale;
            batch.draw(coinTex, p.x - size/2, p.y - size/2, size, size);

            batch.setColor(oldColor);
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
