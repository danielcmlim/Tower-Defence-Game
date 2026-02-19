package com.badlogic.drop;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class ProjectileSystem {

    private final Array<Projectile> projectiles = new Array<>();

    private final Texture projectileTex;
    private final Texture pierceTex;

    private static final float PROJECTILE_W     = 12f;
    private static final float PROJECTILE_H     = 8f;
    private static final float PROJECTILE_SPEED = 400f;

    public ProjectileSystem() {
        Pixmap pm = new Pixmap(12, 8, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 0.3f, 1f);
        pm.fill();
        projectileTex = new Texture(pm);
        pm.dispose();

        pm = new Pixmap(14, 6, Pixmap.Format.RGBA8888);
        pm.setColor(0.2f, 1f, 0.9f, 1f);
        pm.fill();
        pierceTex = new Texture(pm);
        pm.dispose();
    }

    public void shoot(float x, float y, boolean facingRight, boolean pierces) {
        Projectile p = new Projectile();
        p.x       = x;
        p.y       = y;
        p.vx      = facingRight ? PROJECTILE_SPEED : -PROJECTILE_SPEED;
        p.pierces = pierces;
        projectiles.add(p);
    }

    public void shoot(float x, float y, boolean facingRight) {
        shoot(x, y, facingRight, false);
    }

    public void update(float dt, float screenWidth, float groundY, Array<Platform> platforms) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.x += p.vx * dt;

            if (p.y <= groundY) { projectiles.removeIndex(i); continue; }

            boolean hitPlatform = false;
            for (Platform plat : platforms) {
                if (new Rectangle(p.x, p.y, PROJECTILE_W, PROJECTILE_H)
                    .overlaps(new Rectangle(plat.x, plat.y, plat.w, plat.h))) {
                    hitPlatform = true;
                    break;
                }
            }
            if (hitPlatform) { projectiles.removeIndex(i); continue; }

            if (p.x > screenWidth || p.x + PROJECTILE_W < 0) {
                projectiles.removeIndex(i);
            }
        }
    }

    public void update(float dt, float screenWidth) {
        update(dt, screenWidth, -99999f, new Array<>());
    }

    public void checkCollisions(
        Array<Enemy> enemies,
        float enemyW, float enemyH,
        DamageTextSystem damageTextSystem,
        ParticleSystem particleSystem,
        int damage,
        ShopSystem shopSystem,
        EnemySystem enemySystem
    ) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            if (i >= projectiles.size) continue;
            Projectile p = projectiles.get(i);
            Rectangle pRect = new Rectangle(p.x, p.y, PROJECTILE_W, PROJECTILE_H);
            boolean consumed = false;

            for (int j = enemies.size - 1; j >= 0; j--) {
                if (j >= enemies.size) continue;
                Enemy e = enemies.get(j);

                if (pRect.overlaps(new Rectangle(e.x, e.y, enemyW, enemyH))) {
                    if (!p.hitEnemies.contains(e)) {
                        p.hitEnemies.add(e);
                        enemySystem.hitEnemy(e, damage, damageTextSystem, particleSystem, shopSystem);
                    }

                    if (!p.pierces) {
                        projectiles.removeIndex(i);
                        consumed = true;
                        break;
                    }
                }
            }

            if (consumed) continue;
        }
    }

    public void draw(SpriteBatch batch) {
        for (Projectile p : projectiles) {
            batch.draw(p.pierces ? pierceTex : projectileTex, p.x, p.y, PROJECTILE_W, PROJECTILE_H);
        }
    }

    public void dispose() {
        if (projectileTex != null) projectileTex.dispose();
        if (pierceTex     != null) pierceTex.dispose();
    }

    static class Projectile {
        float x, y, vx;
        boolean pierces = false;
        final java.util.HashSet<Enemy> hitEnemies = new java.util.HashSet<>();
    }
}
