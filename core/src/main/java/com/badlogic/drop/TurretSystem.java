package com.badlogic.drop;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class TurretSystem {

    public static class Turret {
        public float x, y;
        public float shootTimer;
        public static final float W = 24f, H = 28f;

        public Turret(float x, float y) {
            this.x = x;
            this.y = y;
            this.shootTimer = MathUtils.random(1f, 3f);
        }
    }

    static class TurretBullet {
        float x, y, vx, vy;
        static final float W = 10f, H = 6f;
        static final float SPEED = 380f;
    }

    private final Array<Turret> turrets = new Array<>();
    private final Array<TurretBullet> bullets = new Array<>();

    private final Texture turretTex;
    private final Texture bulletTex;

    private static final float MIN_INTERVAL = 3f;
    private static final float MAX_INTERVAL = 5f;
    private static final int   BULLET_DAMAGE = 10;

    public TurretSystem() {
        Pixmap pm = new Pixmap((int) Turret.W, (int) Turret.H, Pixmap.Format.RGBA8888);
        pm.setColor(0.15f, 0.75f, 0.25f, 1f);
        pm.fill();
        pm.setColor(0.05f, 0.45f, 0.1f, 1f);
        pm.fillRectangle((int) Turret.W - 6, (int) Turret.H / 2 - 3, 6, 6);
        turretTex = new Texture(pm);
        pm.dispose();

        pm = new Pixmap((int) TurretBullet.W, (int) TurretBullet.H, Pixmap.Format.RGBA8888);
        pm.setColor(0.4f, 1f, 0.3f, 1f);
        pm.fill();
        bulletTex = new Texture(pm);
        pm.dispose();
    }

    public void placeTurret(float x, float y) {
        turrets.add(new Turret(x, y));
    }

    public boolean hasTurrets() {
        return turrets.size > 0;
    }

    public void update(float dt, Array<Enemy> enemies, float enemyW, float enemyH,
                       DamageTextSystem damageTextSystem, ParticleSystem particleSystem,
                       ShopSystem shopSystem, EnemySystem enemySystem, float screenWidth,
                       float screenHeight) {

        for (Turret t : turrets) {
            t.shootTimer -= dt;
            if (t.shootTimer <= 0f) {
                // Find closest enemy by straight-line distance
                Enemy target = null;
                float closest = Float.MAX_VALUE;
                for (Enemy e : enemies) {
                    float dx = (e.x + enemyW / 2f) - (t.x + Turret.W / 2f);
                    float dy = (e.y + enemyH / 2f) - (t.y + Turret.H / 2f);
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < closest) {
                        closest = dist;
                        target = e;
                    }
                }

                if (target != null) {
                    float bx = t.x + Turret.W;
                    float by = t.y + Turret.H / 2f - TurretBullet.H / 2f;

                    float dx = (target.x + enemyW / 2f) - bx;
                    float dy = (target.y + enemyH / 2f) - by;
                    float len = (float) Math.sqrt(dx * dx + dy * dy);

                    TurretBullet b = new TurretBullet();
                    b.x  = bx;
                    b.y  = by;
                    b.vx = (dx / len) * TurretBullet.SPEED;
                    b.vy = (dy / len) * TurretBullet.SPEED;
                    bullets.add(b);
                }

                t.shootTimer = MathUtils.random(MIN_INTERVAL, MAX_INTERVAL);
            }
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            TurretBullet b = bullets.get(i);
            b.x += b.vx * dt;
            b.y += b.vy * dt;

            if (b.x > screenWidth || b.x + TurretBullet.W < 0 ||
                b.y > screenHeight || b.y + TurretBullet.H < 0) {
                bullets.removeIndex(i);
                continue;
            }

            Rectangle bRect = new Rectangle(b.x, b.y, TurretBullet.W, TurretBullet.H);
            boolean consumed = false;

            for (int j = enemies.size - 1; j >= 0; j--) {
                if (j >= enemies.size) continue;
                Enemy e = enemies.get(j);

                if (bRect.overlaps(new Rectangle(e.x, e.y, enemyW, enemyH))) {
                    if (e.type == Enemy.Type.TANK) {
                        damageTextSystem.addText(e.x + enemyW / 2f, e.y + enemyH + 10f, "BLOCKED");
                        bullets.removeIndex(i);
                        consumed = true;
                    } else {
                        enemySystem.hitEnemy(e, BULLET_DAMAGE, false,
                            damageTextSystem, particleSystem, shopSystem);
                        bullets.removeIndex(i);
                        consumed = true;
                    }
                    break;
                }
            }
        }
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapes) {
        batch.begin();
        for (Turret t : turrets) {
            batch.draw(turretTex, t.x, t.y, Turret.W, Turret.H);
        }
        for (TurretBullet b : bullets) {
            batch.draw(bulletTex, b.x, b.y, TurretBullet.W, TurretBullet.H);
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Turret t : turrets) {
            float barW = Turret.W;
            float barH = 3f;
            float barX = t.x;
            float barY = t.y + Turret.H + 4f;
            float pct  = 1f - (t.shootTimer / MAX_INTERVAL);

            shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
            shapes.rect(barX, barY, barW, barH);
            shapes.setColor(0.3f, 1f, 0.3f, 1f);
            shapes.rect(barX, barY, barW * MathUtils.clamp(pct, 0f, 1f), barH);
        }
        shapes.end();
    }

    public void dispose() {
        if (turretTex != null) turretTex.dispose();
        if (bulletTex != null) bulletTex.dispose();
    }
}
