package com.badlogic.drop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class ProjectileSystem {

    private Array<Projectile> projectiles = new Array<>();
    private Texture projectileTex;
    private static final float PROJECTILE_W = 12f;
    private static final float PROJECTILE_H = 8f;
    private static final float PROJECTILE_SPEED = 400f;

    public ProjectileSystem() {
        // Create projectile texture
        Pixmap pm = new Pixmap(12, 8, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 0.3f, 1f);
        pm.fill();
        projectileTex = new Texture(pm);
        pm.dispose();
    }

    public void shoot(float x, float y, boolean facingRight) {
        Projectile p = new Projectile();
        p.x = x;
        p.y = y;
        p.facingRight = facingRight;
        projectiles.add(p);
    }

    public void update(float dt, float screenWidth) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);

            float speed = p.facingRight ? PROJECTILE_SPEED : -PROJECTILE_SPEED;
            p.x += speed * dt;

            // Remove if off screen
            if (p.x > screenWidth || p.x + PROJECTILE_W < 0) {
                projectiles.removeIndex(i);
            }
        }
    }

    public void checkCollisions(
        Array<Enemy> enemies,
        float enemyW,
        float enemyH,
        DamageTextSystem damageTextSystem,
        ParticleSystem particleSystem,
        int baseDamage,
        ShopSystem shopSystem
    ) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            Rectangle pRect = new Rectangle(p.x, p.y, PROJECTILE_W, PROJECTILE_H);

            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                Rectangle eRect = new Rectangle(e.x, e.y, enemyW, enemyH);

                if (pRect.overlaps(eRect)) {
                    // Hit!
                    damageTextSystem.add(e.x + enemyW / 2f, e.y + enemyH + 10f, -baseDamage);

                    // Spawn coin particles and award coins
                    float centerX = e.x + enemyW / 2f;
                    float centerY = e.y + enemyH / 2f;
                    int coinReward = MathUtils.random(3, 6);
                    particleSystem.spawnCoinBurst(centerX, centerY, coinReward);
                    shopSystem.addCoins(coinReward);

                    enemies.removeIndex(j);
                    projectiles.removeIndex(i);
                    break;
                }
            }
        }
    }

    public void draw(SpriteBatch batch) {
        for (Projectile p : projectiles) {
            batch.draw(projectileTex, p.x, p.y, PROJECTILE_W, PROJECTILE_H);
        }
    }

    public void dispose() {
        if (projectileTex != null) projectileTex.dispose();
    }

    static class Projectile {
        float x, y;
        boolean facingRight;
    }
}
