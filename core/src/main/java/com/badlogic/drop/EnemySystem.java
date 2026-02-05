package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class EnemySystem {

    private final Array<Enemy> enemies = new Array<>();

    private final Texture enemyTex;
    private final float enemyW, enemyH;
    private final float enemySpeed;
    private final int enemyContactDamage;

    private float spawnTimer = 0f;
    private final float spawnInterval;

    private int wave = 0;
    private int toSpawnThisWave = 0;
    private int spawnedThisWave = 0;

    private float prepTimer = 0f;
    private float prepDuration = 2.0f;

    private boolean inPrep = true;


    public EnemySystem(Texture enemyTex, float enemyW, float enemyH, float enemySpeed, int enemyContactDamage, float spawnInterval) {
        this.enemyTex = enemyTex;
        this.enemyW = enemyW;
        this.enemyH = enemyH;
        this.enemySpeed = enemySpeed;
        this.enemyContactDamage = enemyContactDamage;
        this.spawnInterval = spawnInterval;
    }

    public void updateAndSpawn(float dt, float[] laneY) {
        if (inPrep) {
            prepTimer -= dt;
            if (prepTimer <= 0f) {
                beginWave();
            }
            return;
        }

        // If we finished spawning AND all enemies are dead, go to next prep
        if (spawnedThisWave >= toSpawnThisWave && enemies.size == 0) {
            beginPrep();
            return;
        }

        // Spawn until we hit the wave quota
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

        Enemy e = new Enemy();
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
                float left = e.x;
                float right = e.x + enemyW;

                for (Platform p : platforms) {
                    float top = p.top();
                    boolean wasAbove = prevY >= top;
                    boolean nowBelowTop = e.y <= top;
                    boolean overlapX = right > p.x && left < p.x + p.w;

                    if (wasAbove && nowBelowTop && overlapX) {
                        e.y = top;
                        e.vy = 0f;
                        break;
                    }
                }
            }

            e.x -= enemySpeed * dt;

            if (attackActive) {
                boolean hit =
                    attackBounds.x < e.x + enemyW &&
                        attackBounds.x + attackBounds.width > e.x &&
                        attackBounds.y < e.y + enemyH &&
                        attackBounds.y + attackBounds.height > e.y;

                if (hit) {
                    damageTextSystem.add(e.x + enemyW / 2f, e.y + enemyH + 10f, -attackDamage);

                    // Spawn coin particles and award coins
                    float centerX = e.x + enemyW / 2f;
                    float centerY = e.y + enemyH / 2f;
                    int coinReward = MathUtils.random(3, 6);
                    particleSystem.spawnCoinBurst(centerX, centerY, coinReward);
                    shopSystem.addCoins(coinReward);

                    enemies.removeIndex(i);
                    continue;
                }
            }

            boolean touchesBase =
                e.x <= (baseX + baseW) &&
                    (e.y < baseY + baseH) && ((e.y + enemyH) > baseY);

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

    public int getWave() { return wave; }
    public boolean isInPrep() { return inPrep; }
    public float getPrepTimer() { return prepTimer; }
    public float getPrepDuration() { return prepDuration; }
    public int getRemainingToSpawn() { return Math.max(0, toSpawnThisWave - spawnedThisWave); }
    public int getAliveCount() { return enemies.size; }

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
        toSpawnThisWave = 3 + wave * 2;     // wave 1=5, wave 2=7, etc. tweak as you like
        spawnedThisWave = 0;
        spawnTimer = 0f;                   // spawn immediately
    }


    public void draw(SpriteBatch batch) {
        for (Enemy e : enemies) {
            batch.draw(enemyTex, e.x, e.y, enemyW, enemyH);
        }
    }
}
