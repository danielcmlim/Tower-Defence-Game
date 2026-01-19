package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Main implements ApplicationListener {

    private SpriteBatch batch;
    private ShapeRenderer shapes;

    private Texture playerTex;
    private float px = 160, py = 64;
    private float vx = 0, vy = 0;
    private boolean onGround = true;

    private static final float GROUND_Y   = 64f;
    private static final float MOVE_ACCEL = 900f;
    private static final float MOVE_MAX   = 220f;
    private static final float FRICTION   = 1400f;
    private static final float GRAVITY    = -900f;
    private static final float JUMP_VY    = 380f;

    private Texture baseTex;
    private float baseX = 40, baseY = GROUND_Y;
    private float baseW = 48, baseH = 32;

    private int baseHp = 100;
    private int baseHpMax = 100;

    private Stage stage;
    private Skin skin;
    private BitmapFont font;

    private Texture enemyTex;
    private float enemyW = 28, enemyH = 28;
    private float enemySpeed = 60f;
    private int enemyContactDamage = 10;
    private float enemySpawnInterval = 1.5f;

    private boolean attackActive = false;
    private float attackTimer = 0f;
    private float attackDuration = 0.15f;
    private float attackCooldown = 0.30f;
    private float attackCooldownTimer = 0f;
    private int attackDamage = 10;

    private boolean facingRight = true;
    private boolean dropThroughPlatform = false;

    private static final float PLAYER_W = 32f;
    private static final float PLAYER_H = 32f;

    private static final float ATTACK_W = 20f;
    private static final float ATTACK_H = 24f;

    private final Rectangle attackBounds = new Rectangle();

    private Array<Platform> platforms = new Array<>();
    private float[] laneY;

    private DamageTextSystem damageTextSystem;
    private EnemySystem enemySystem;
    private ShopSystem shopSystem;
    private ProjectileSystem projectileSystem;
    private ParticleSystem particleSystem;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        try {
            playerTex = new Texture("player.png");
        } catch (Exception e) {
            playerTex = solid(32, 32, Color.WHITE);
        }

        baseTex = solid((int) baseW, (int) baseH, new Color(0.2f, 0.5f, 1f, 1f));

        platforms.add(new Platform(340, GROUND_Y + 50, 300, 8));
        platforms.add(new Platform(440, GROUND_Y + 120, 200, 8));

        laneY = new float[] {
            GROUND_Y,
            platforms.get(0).top(),
            platforms.get(1).top()
        };

        stage = new Stage(new ScreenViewport());
        font = new BitmapFont();
        skin = new Skin();

        Texture upTex   = solid(150, 40, new Color(0.18f, 0.18f, 0.18f, 1f));
        Texture downTex = solid(150, 40, new Color(0.12f, 0.12f, 0.12f, 1f));
        Texture overTex = solid(150, 40, new Color(0.24f, 0.24f, 0.24f, 1f));
        skin.add("up", upTex);
        skin.add("down", downTex);
        skin.add("over", overTex);
        skin.add("font", font);

        Pixmap epm = new Pixmap((int) enemyW, (int) enemyH, Pixmap.Format.RGBA8888);
        epm.setColor(1f, 0.2f, 0.2f, 1f);
        epm.fill();
        enemyTex = new Texture(epm);
        epm.dispose();

        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = skin.newDrawable("up");
        tbs.down = skin.newDrawable("down");
        tbs.over = skin.newDrawable("over");
        tbs.font = font;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        damageTextSystem = new DamageTextSystem();
        enemySystem = new EnemySystem(enemyTex, enemyW, enemyH, enemySpeed, enemyContactDamage, enemySpawnInterval);
        enemySystem.startFirstWave();

        projectileSystem = new ProjectileSystem();
        particleSystem = new ParticleSystem();

        shopSystem = new ShopSystem(stage, skin, new ShopSystem.ShopCallback() {
            @Override
            public void onRangedAttackPurchased() {
                System.out.println("You can now shoot projectiles with K!");
            }

            @Override
            public void onFasterAttackPurchased() {
                attackCooldown = 0.15f; // Reduced from 0.30f
                System.out.println("Attack cooldown reduced!");
            }

            @Override
            public void onStrongerAttackPurchased() {
                attackDamage = 15; // Increased from 10
                System.out.println("Attack damage increased!");
            }

            @Override
            public void onRepairBase() {
                int healAmount = baseHpMax - baseHp;
                baseHp = baseHpMax;
                float centerX = baseX + baseW / 2f;
                float centerY = baseY + baseH + 18f;
                damageTextSystem.add(centerX, centerY, healAmount);
            }

            @Override
            public int getBaseHp() {
                return baseHp;
            }

            @Override
            public int getBaseHpMax() {
                return baseHpMax;
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    private Texture solid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private void changeBaseHp(int delta) {
        if (delta == 0) return;

        baseHp = MathUtils.clamp(baseHp + delta, 0, baseHpMax);

        float centerX = baseX + baseW / 2f;
        float centerY = baseY + baseH + 18f;
        damageTextSystem.add(centerX, centerY, delta);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // Check for shop toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            shopSystem.toggle();
        }

        // Only process game input if shop is closed
        if (!shopSystem.isOpen()) {
            boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            boolean jump  = Gdx.input.isKeyJustPressed(Input.Keys.UP);
            boolean down  = Gdx.input.isKeyPressed(Input.Keys.DOWN);
            boolean attackKey = Gdx.input.isKeyJustPressed(Input.Keys.J);
            boolean shootKey = Gdx.input.isKeyJustPressed(Input.Keys.K);

            // Drop through platform when holding down
            if (down && onGround && py > GROUND_Y) {
                dropThroughPlatform = true;
                py -= 5f; // Drop slightly to trigger fall
                onGround = false;
            } else if (!down) {
                dropThroughPlatform = false;
            }

            if (left ^ right) {
                float dir = right ? 1f : -1f;
                vx += dir * MOVE_ACCEL * dt;
                if (right) facingRight = true;
                if (left)  facingRight = false;
            } else {
                if (vx > 0) vx = Math.max(0, vx - FRICTION * dt);
                else if (vx < 0) vx = Math.min(0, vx + FRICTION * dt);
            }
            vx = MathUtils.clamp(vx, -MOVE_MAX, MOVE_MAX);

            if (jump && onGround) {
                vy = JUMP_VY;
                onGround = false;
            }

            float prevPy = py;

            vy += GRAVITY * dt;
            px += vx * dt;
            py += vy * dt;

            onGround = false;

            if (py <= GROUND_Y) {
                py = GROUND_Y;
                vy = 0;
                onGround = true;
            }

            if (vy <= 0f) {
                float playerBottomPrev = prevPy;
                float playerBottomNow  = py;
                float playerLeft = px;
                float playerRight = px + PLAYER_W;

                for (Platform p : platforms) {
                    // Skip platform collision if dropping through
                    if (dropThroughPlatform) continue;

                    float platTop = p.top();
                    boolean wasAbove = playerBottomPrev >= platTop;
                    boolean nowBelowTop = playerBottomNow <= platTop;
                    boolean overlapX = playerRight > p.x && playerLeft < p.x + p.w;

                    if (wasAbove && nowBelowTop && overlapX) {
                        py = platTop;
                        vy = 0;
                        onGround = true;
                        break;
                    }
                }
            }

            if (attackCooldownTimer > 0f) attackCooldownTimer -= dt;

            if (attackTimer > 0f) {
                attackTimer -= dt;
                if (attackTimer <= 0f) attackActive = false;
            }

            // Melee attack (J key)
            if (attackKey && attackCooldownTimer <= 0f) {
                attackActive = true;
                attackTimer = attackDuration;
                attackCooldownTimer = attackCooldown;
            }

            // Ranged attack (K key) - only if purchased
            if (shootKey && attackCooldownTimer <= 0f && shopSystem.hasRangedAttack()) {
                float shootX = facingRight ? (px + PLAYER_W) : px;
                float shootY = py + PLAYER_H / 2f;
                projectileSystem.shoot(shootX, shootY, facingRight);
                attackCooldownTimer = attackCooldown;
            }

            float attackY = py + (PLAYER_H - ATTACK_H) / 2f;
            float attackX = facingRight ? (px + PLAYER_W) : (px - ATTACK_W);
            attackBounds.set(attackX, attackY, ATTACK_W, ATTACK_H);
        }

        damageTextSystem.update(dt);
        projectileSystem.update(dt, Gdx.graphics.getWidth());
        particleSystem.update(dt);

        enemySystem.updateAndSpawn(dt, laneY);

        int baseDelta = enemySystem.updateEnemies(
            dt,
            GRAVITY,
            GROUND_Y,
            platforms,
            baseX, baseY, baseW, baseH,
            attackBounds,
            attackActive,
            damageTextSystem,
            particleSystem,
            attackDamage,
            shopSystem
        );

        // Check projectile collisions
        projectileSystem.checkCollisions(
            enemySystem.getEnemies(),
            enemyW,
            enemyH,
            damageTextSystem,
            particleSystem,
            attackDamage,
            shopSystem
        );

        if (baseDelta != 0) changeBaseHp(baseDelta);

        stage.act(dt);

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        batch.draw(baseTex, baseX, baseY, baseW, baseH);
        batch.draw(playerTex, px, py);

        enemySystem.draw(batch);
        projectileSystem.draw(batch);
        particleSystem.draw(batch);

        String waveText;
        if (enemySystem.isInPrep()) {
            waveText = "Wave " + (enemySystem.getWave() + 1) + " in " +
                (int)Math.ceil(enemySystem.getPrepTimer()) + "s";
        } else {
            waveText = "Wave " + enemySystem.getWave() +
                " | Alive: " + enemySystem.getAliveCount() +
                " | Left to spawn: " + enemySystem.getRemainingToSpawn();
        }

        font.draw(batch, waveText, 12, Gdx.graphics.getHeight() - 12);
        font.draw(batch, "Controls: A/D - Move | UP - Jump | J - Melee | K - Shoot | S - Shop",
            12, Gdx.graphics.getHeight() - 32);

        damageTextSystem.draw(batch, font);

        batch.end();

        float barWidth = 64f;
        float barHeight = 8f;
        float barX = baseX - 8;
        float barY = baseY + baseH + 6;

        float pct = (float) baseHp / (float) baseHpMax;
        float fgW = barWidth * pct;

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapes.rect(barX, barY, barWidth, barHeight);

        shapes.setColor(0.2f, 0.85f, 0.3f, 1f);
        shapes.rect(barX, barY, fgW, barHeight);

        shapes.setColor(0.35f, 0.35f, 0.35f, 1f);
        for (Platform p : platforms) {
            shapes.rect(p.x, p.y, p.w, p.h);
        }

        shapes.end();

        if (attackActive) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.YELLOW);
            shapes.rect(attackBounds.x, attackBounds.y, attackBounds.width, attackBounds.height);
            shapes.end();
        }

        stage.draw();
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (playerTex != null) playerTex.dispose();
        if (baseTex != null) baseTex.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
        if (enemyTex != null) enemyTex.dispose();
        if (projectileSystem != null) projectileSystem.dispose();
        if (particleSystem != null) particleSystem.dispose();
    }
}
