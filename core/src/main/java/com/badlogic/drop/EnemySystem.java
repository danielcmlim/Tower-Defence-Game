package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class EnemySystem {

    private final Array<Enemy> enemies = new Array<>();

    // Sniper projectiles are managed here so they can damage the base
    public static class SniperProjectile {
        public float x, y;
        public float vx, vy;
        public boolean active = true;
        public static final float W = 8f, H = 8f;
    }
    private final Array<SniperProjectile> sniperProjectiles = new Array<>();

    private final Texture enemyTex;
    private final Texture shieldTex;
    private final Texture sniperTex;
    private final Texture tankTex;
    private final Texture sniperBulletTex;

    private final float enemyW, enemyH;
    private final float enemySpeed;
    private final int enemyContactDamage;

    private float spawnTimer = 0f;
    private final float spawnInterval;

    private int wave = 0;
    private int toSpawnThisWave = 0;
    private int spawnedThisWave = 0;

    private float prepTimer = 0f;
    private final float prepDuration = 1.5f;
    private boolean inPrep = true;

    public EnemySystem(Texture enemyTex, float enemyW, float enemyH,
                       float enemySpeed, int enemyContactDamage, float spawnInterval) {
        this.enemyTex = enemyTex;
        this.enemyW = enemyW;
        this.enemyH = enemyH;
        this.enemySpeed = enemySpeed;
        this.enemyContactDamage = enemyContactDamage;
        this.spawnInterval = spawnInterval;

        // Shield overlay — blue
        Pixmap pm = new Pixmap((int) enemyW + 8, (int) enemyH + 8, Pixmap.Format.RGBA8888);
        pm.setColor(0.2f, 0.5f, 1f, 0.45f);
        pm.fill();
        shieldTex = new Texture(pm);
        pm.dispose();

        // Sniper — dark purple
        pm = new Pixmap((int) enemyW, (int) enemyH, Pixmap.Format.RGBA8888);
        pm.setColor(0.5f, 0.1f, 0.8f, 1f);
        pm.fill();
        sniperTex = new Texture(pm);
        pm.dispose();

        // Tank — dark grey
        pm = new Pixmap((int) enemyW + 6, (int) enemyH + 6, Pixmap.Format.RGBA8888);
        pm.setColor(0.3f, 0.3f, 0.35f, 1f);
        pm.fill();
        tankTex = new Texture(pm);
        pm.dispose();

        // Sniper bullet — bright magenta dot
        pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 0.2f, 0.8f, 1f);
        pm.fillCircle(4, 4, 3);
        sniperBulletTex = new Texture(pm);
        pm.dispose();
    }

    public void updateAndSpawn(float dt, float[] laneY) {
        if (inPrep) {
            prepTimer -= dt;
            if (prepTimer <= 0f) beginWave();
            return;
        }

        if (spawnedThisWave >= toSpawnThisWave && enemies.size == 0) {
            beginPrep();
            return;
        }

        if (spawnedThisWave < toSpawnThisWave) {
            spawnTimer -= dt;
            if (spawnTimer <= 0f) {
                spawnEnemy(laneY);
                spawnedThisWave++;
                spawnTimer = spawnInterval;
            }
        }
    }

    private void spawnEnemy(float[] laneY) {
        float screenW = Gdx.graphics.getWidth();

        // Special enemy chance scales with wave — 10% each type from wave 2+
        float specialChance = wave >= 2 ? 0.15f : 0f;
        float roll = MathUtils.random();

        if (roll < specialChance) {
            // SNIPER — spawns on the top platform (laneY[last]), stays still and shoots
            spawnSniper(screenW, laneY[laneY.length - 1]);
        } else if (roll < specialChance * 2) {
            // TANK — 5 HP, blocks pierce, spawns on ground
            spawnTank(screenW, laneY[0]);
        } else {
            // NORMAL
            int lane = MathUtils.random(0, laneY.length - 1);
            int hp = 1 + MathUtils.random(0, Math.min(wave - 1, 5));
            boolean hasShield = wave >= 1 && MathUtils.random() < (wave >= 2 ? 0.5f : 0.2f);
            Enemy e = new Enemy(hp, hasShield, Enemy.Type.NORMAL);
            e.x = screenW - enemyW - 10;
            e.y = laneY[lane];
            e.vy = 0f;
            enemies.add(e);
        }
    }

    private void spawnSniper(float screenW, float topLaneY) {
        Enemy e = new Enemy(2, false, Enemy.Type.SNIPER);
        e.x = screenW - enemyW - 10;
        e.y = topLaneY;
        e.vy = 0f;
        e.shootCooldown = Enemy.SNIPER_SHOOT_INTERVAL;
        enemies.add(e);
    }

    private void spawnTank(float screenW, float groundY) {
        Enemy e = new Enemy(30, false, Enemy.Type.TANK);
        e.x = screenW - enemyW - 10;
        e.y = groundY;
        e.vy = 0f;
        enemies.add(e);
    }

    public int updateEnemies(
        float dt,
        float gravity,
        float groundY,
        Array<Platform> platforms,
        float baseX, float baseY, float baseW, float baseH,
        Rectangle attackBounds,
        boolean attackActive,
        DamageTextSystem damageTextSystem,
        ParticleSystem particleSystem,
        int attackDamage,
        ShopSystem shopSystem
    ) {
        int baseDelta = 0;

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);

            // --- Sniper: stay put, shoot at base ---
            if (e.type == Enemy.Type.SNIPER) {
                e.shootCooldown -= dt;
                if (e.shootCooldown <= 0f) {
                    fireSniper(e, baseX + baseW / 2f, baseY + baseH / 2f);
                    e.shootCooldown = Enemy.SNIPER_SHOOT_INTERVAL;
                }
                // Snipers still walk slowly toward base but mainly stay back
                e.x -= (enemySpeed * 0.3f) * dt;
            } else {
                // --- Normal gravity & movement ---
                float prevY = e.y;
                e.vy += gravity * dt;
                e.y  += e.vy * dt;

                if (e.y <= groundY) {
                    e.y = groundY;
                    e.vy = 0f;
                }

                if (e.vy <= 0f) {
                    float left  = e.x;
                    float right = e.x + enemyW;
                    for (Platform p : platforms) {
                        float top = p.top();
                        boolean wasAbove = prevY >= top;
                        boolean nowBelow = e.y   <= top;
                        boolean overlapX = right > p.x && left < p.x + p.w;
                        if (wasAbove && nowBelow && overlapX) {
                            e.y  = top;
                            e.vy = 0f;
                            break;
                        }
                    }
                }

                float speed = e.type == Enemy.Type.TANK ? enemySpeed * 0.6f : enemySpeed;
                e.x -= speed * dt;
            }

            // --- Melee hit ---
            if (!attackActive) {
                e.hitThisSwing = false;
            }

            if (attackActive && !e.hitThisSwing) {
                boolean hit =
                    attackBounds.x                       < e.x + enemyW &&
                        attackBounds.x + attackBounds.width  > e.x &&
                        attackBounds.y                       < e.y + enemyH &&
                        attackBounds.y + attackBounds.height > e.y;

                if (hit) {
                    e.hitThisSwing = true;
                    boolean dead = e.takeDamage(attackDamage);
                    damageTextSystem.add(e.x + enemyW / 2f, e.y + enemyH + 10f, -attackDamage);
                    if (dead) {
                        awardKill(e, particleSystem, shopSystem);
                        enemies.removeIndex(i);
                        continue;
                    }
                }
            }

            // --- Base contact ---
            boolean touchesBase =
                e.x <= (baseX + baseW) &&
                    e.y  <  baseY + baseH  &&
                    (e.y + enemyH) > baseY;

            if (touchesBase) {
                baseDelta -= enemyContactDamage;
                enemies.removeIndex(i);
                continue;
            }

            if (e.x + enemyW < 0) {
                enemies.removeIndex(i);
            }
        }

        // --- Sniper projectiles ---
        for (int i = sniperProjectiles.size - 1; i >= 0; i--) {
            SniperProjectile sp = sniperProjectiles.get(i);
            sp.x += sp.vx * dt;
            sp.y += sp.vy * dt;

            // Hit base
            boolean hitsBase =
                sp.x < baseX + baseW && sp.x + SniperProjectile.W > baseX &&
                    sp.y < baseY + baseH && sp.y + SniperProjectile.H > baseY;

            if (hitsBase) {
                baseDelta -= 10;
                sniperProjectiles.removeIndex(i);
                continue;
            }

            // Cull off-screen
            if (sp.x + SniperProjectile.W < 0 || sp.x > Gdx.graphics.getWidth() ||
                sp.y + SniperProjectile.H < 0 || sp.y > Gdx.graphics.getHeight()) {
                sniperProjectiles.removeIndex(i);
            }
        }

        return baseDelta;
    }

    private void fireSniper(Enemy e, float targetX, float targetY) {
        SniperProjectile sp = new SniperProjectile();
        sp.x = e.x;
        sp.y = e.y + enemyH / 2f;
        float dx = targetX - sp.x;
        float dy = targetY - sp.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float speed = 300f;
        sp.vx = (dx / len) * speed;
        sp.vy = (dy / len) * speed;
        sniperProjectiles.add(sp);
    }

    public boolean hitEnemy(
        Enemy e,
        int damage,
        boolean isPierce,
        DamageTextSystem damageTextSystem,
        ParticleSystem particleSystem,
        ShopSystem shopSystem
    ) {
        // Tank is immune to all projectiles — melee only
        if (e.type == Enemy.Type.TANK) return false;

        boolean dead = e.takeDamage(damage);
        damageTextSystem.add(e.x + enemyW / 2f, e.y + enemyH + 10f, -damage);
        if (dead) {
            awardKill(e, particleSystem, shopSystem);
            enemies.removeValue(e, true);
        }
        return dead;
    }

    private void awardKill(Enemy e, ParticleSystem particleSystem, ShopSystem shopSystem) {
        float cx = e.x + enemyW / 2f;
        float cy = e.y + enemyH / 2f;
        int coins = MathUtils.random(1, 2);
        if (e.type == Enemy.Type.SNIPER) coins += 3;
        if (e.type == Enemy.Type.TANK)   coins += 4;
        particleSystem.spawnCoinBurst(cx, cy, coins);
        shopSystem.addCoins(coins);
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapes) {
        batch.begin();
        for (Enemy e : enemies) {
            if (e.type == Enemy.Type.SNIPER) {
                batch.draw(sniperTex, e.x, e.y, enemyW, enemyH);
            } else if (e.type == Enemy.Type.TANK) {
                batch.draw(tankTex, e.x - 3, e.y - 3, enemyW + 6, enemyH + 6);
            } else {
                batch.draw(enemyTex, e.x, e.y, enemyW, enemyH);
                if (e.hasShield) {
                    batch.draw(shieldTex, e.x - 4, e.y - 4, enemyW + 8, enemyH + 8);
                }
            }
        }
        // Sniper bullets
        for (SniperProjectile sp : sniperProjectiles) {
            batch.draw(sniperBulletTex, sp.x, sp.y, SniperProjectile.W, SniperProjectile.H);
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            float barW = e.type == Enemy.Type.TANK ? enemyW + 6 : enemyW;
            float barX = e.type == Enemy.Type.TANK ? e.x - 3 : e.x;
            float barH = 4f;
            float barY = e.y + enemyH + (e.type == Enemy.Type.TANK ? 3 : 0) + 4f;

            shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
            shapes.rect(barX, barY, barW, barH);

            float hpPct = (float) e.hp / e.maxHp;
            if (e.type == Enemy.Type.TANK) {
                shapes.setColor(0.4f, 0.4f, 0.45f, 1f);
            } else if (e.type == Enemy.Type.SNIPER) {
                shapes.setColor(0.6f, 0.2f, 0.9f, 1f);
            } else {
                shapes.setColor(0.2f + 0.8f * (1f - hpPct), 0.2f + 0.65f * hpPct, 0.2f, 1f);
            }
            shapes.rect(barX, barY, barW * hpPct, barH);

            if (e.hasShield) {
                float shieldBarY = barY + barH + 2f;
                shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
                shapes.rect(barX, shieldBarY, barW, barH);
                shapes.setColor(0.2f, 0.5f, 1f, 1f);
                shapes.rect(barX, shieldBarY, barW, barH);
            }
        }
        shapes.end();
    }

    public void draw(SpriteBatch batch) {
        for (Enemy e : enemies) {
            if (e.type == Enemy.Type.SNIPER) {
                batch.draw(sniperTex, e.x, e.y, enemyW, enemyH);
            } else if (e.type == Enemy.Type.TANK) {
                batch.draw(tankTex, e.x - 3, e.y - 3, enemyW + 6, enemyH + 6);
            } else {
                batch.draw(enemyTex, e.x, e.y, enemyW, enemyH);
                if (e.hasShield) batch.draw(shieldTex, e.x - 4, e.y - 4, enemyW + 8, enemyH + 8);
            }
        }
        for (SniperProjectile sp : sniperProjectiles) {
            batch.draw(sniperBulletTex, sp.x, sp.y, SniperProjectile.W, SniperProjectile.H);
        }
    }

    public int getWave()             { return wave; }
    public boolean isInPrep()        { return inPrep; }
    public float getPrepTimer()      { return prepTimer; }
    public float getPrepDuration()   { return prepDuration; }
    public int getRemainingToSpawn() { return Math.max(0, toSpawnThisWave - spawnedThisWave); }
    public int getAliveCount()       { return enemies.size; }
    public Array<Enemy> getEnemies() { return enemies; }

    public void startFirstWave() {
        wave = 0;
        beginPrep();
    }

    private void beginPrep() {
        inPrep = true;
        prepTimer = prepDuration;
        toSpawnThisWave = 0;
        spawnedThisWave = 0;
    }

    private void beginWave() {
        inPrep = false;
        wave++;
        toSpawnThisWave = 5 + wave * 4;
        spawnedThisWave = 0;
        spawnTimer = 0f;
    }

    public void dispose() {
        if (shieldTex      != null) shieldTex.dispose();
        if (sniperTex      != null) sniperTex.dispose();
        if (tankTex        != null) tankTex.dispose();
        if (sniperBulletTex != null) sniperBulletTex.dispose();
    }
}
