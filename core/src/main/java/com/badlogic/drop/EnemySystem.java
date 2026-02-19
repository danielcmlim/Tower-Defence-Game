package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class EnemySystem {

    private final Array<Enemy> enemies = new Array<>();

    private final Texture enemyTex;
    private final Texture shieldTex;
    private final float enemyW, enemyH;
    private final float enemySpeed;
    private final int enemyContactDamage;

    private float spawnTimer = 0f;
    private final float spawnInterval;

    private int wave = 0;
    private int toSpawnThisWave = 0;
    private int spawnedThisWave = 0;

    private float prepTimer = 0f;
    private final float prepDuration = 2.0f;
    private boolean inPrep = true;

    public EnemySystem(Texture enemyTex, float enemyW, float enemyH,
                       float enemySpeed, int enemyContactDamage, float spawnInterval) {
        this.enemyTex = enemyTex;
        this.enemyW = enemyW;
        this.enemyH = enemyH;
        this.enemySpeed = enemySpeed;
        this.enemyContactDamage = enemyContactDamage;
        this.spawnInterval = spawnInterval;

        Pixmap pm = new Pixmap((int) enemyW + 8, (int) enemyH + 8, Pixmap.Format.RGBA8888);
        pm.setColor(0.2f, 0.5f, 1f, 0.45f);
        pm.fill();
        shieldTex = new Texture(pm);
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
        int lane = MathUtils.random(0, laneY.length - 1);

        int hp = 1 + MathUtils.random(0, Math.min(wave - 1, 3));
        boolean hasShield = wave >= 2 && MathUtils.random() < 0.3f;

        Enemy e = new Enemy(hp, hasShield);
        e.x = screenW - enemyW - 10;
        e.y = laneY[lane];
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

            e.x -= enemySpeed * dt;

            if (attackActive) {
                boolean hit =
                    attackBounds.x                   < e.x + enemyW &&
                        attackBounds.x + attackBounds.width  > e.x &&
                        attackBounds.y                   < e.y + enemyH &&
                        attackBounds.y + attackBounds.height > e.y;

                if (hit && !e.hitThisSwing) {
                    e.hitThisSwing = true;
                    boolean dead = e.takeDamage(attackDamage);
                    damageTextSystem.add(e.x + enemyW / 2f, e.y + enemyH + 10f, -attackDamage);
                    if (dead) {
                        awardKill(e, particleSystem, shopSystem);
                        enemies.removeIndex(i);
                        continue;
                    }
                }
            } else {
                e.hitThisSwing = false;
            }

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

        return baseDelta;
    }

    public boolean hitEnemy(
        Enemy e,
        int damage,
        DamageTextSystem damageTextSystem,
        ParticleSystem particleSystem,
        ShopSystem shopSystem
    ) {
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
        int coins = MathUtils.random(3, 6) + (e.maxHp - 1);
        particleSystem.spawnCoinBurst(cx, cy, coins);
        shopSystem.addCoins(coins);
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapes) {
        batch.begin();
        for (Enemy e : enemies) {
            batch.draw(enemyTex, e.x, e.y, enemyW, enemyH);
            if (e.hasShield) {
                batch.draw(shieldTex, e.x - 4, e.y - 4, enemyW + 8, enemyH + 8);
            }
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            float barW = enemyW;
            float barH = 4f;
            float barX = e.x;
            float barY = e.y + enemyH + 4f;

            shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
            shapes.rect(barX, barY, barW, barH);

            float hpPct = (float) e.hp / e.maxHp;
            shapes.setColor(0.2f + 0.8f * (1f - hpPct), 0.2f + 0.65f * hpPct, 0.2f, 1f);
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
            batch.draw(enemyTex, e.x, e.y, enemyW, enemyH);
            if (e.hasShield) {
                batch.draw(shieldTex, e.x - 4, e.y - 4, enemyW + 8, enemyH + 8);
            }
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
        toSpawnThisWave = 3 + wave * 2;
        spawnedThisWave = 0;
        spawnTimer = 0f;
    }

    public void dispose() {
        if (shieldTex != null) shieldTex.dispose();
    }
}
