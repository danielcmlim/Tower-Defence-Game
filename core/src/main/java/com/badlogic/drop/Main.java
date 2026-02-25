package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
    private boolean facingRight = true;
    private boolean dropThroughPlatform = false;

    private static final float GROUND_Y   = 64f;
    private static final float MOVE_ACCEL = 900f;
    private static final float MOVE_MAX   = 220f;
    private static final float FRICTION   = 1400f;
    private static final float GRAVITY    = -900f;
    private static final float JUMP_VY    = 380f;
    private static final float PLAYER_W   = 32f;
    private static final float PLAYER_H   = 32f;
    private static final float ATTACK_W   = 20f;
    private static final float ATTACK_H   = 24f;

    private boolean attackActive = false;
    private float attackTimer = 0f;
    private float attackDuration = 0.15f;
    private float attackCooldown = 0.30f;
    private float attackCooldownTimer = 0f;
    private int attackDamage = 10;

    private final Rectangle attackBounds = new Rectangle();

    private Texture baseTex;
    private final float baseX = 40, baseY = GROUND_Y;
    private final float baseW = 48, baseH = 32;
    private int baseHp    = 100;
    private int baseHpMax = 100;

    private final float enemyW             = 28f;
    private final float enemyH             = 28f;
    private final float enemySpeed         = 60f;
    private final int   enemyContactDamage = 10;
    private final float enemySpawnInterval = 0.8f;

    private Array<Platform> platforms = new Array<>();
    private float[] laneY;

    private DamageTextSystem damageTextSystem;
    private EnemySystem      enemySystem;
    private ShopSystem       shopSystem;
    private ProjectileSystem projectileSystem;
    private ParticleSystem   particleSystem;
    private TurretSystem     turretSystem;

    private Stage      stage;
    private Skin       skin;
    private BitmapFont font;
    private BitmapFont bigFont;
    private Texture    enemyTex;

    private boolean gameOver = false;
    private final GlyphLayout layout = new GlyphLayout();

    @Override
    public void create() {
        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();

        font    = new BitmapFont();
        bigFont = new BitmapFont();
        bigFont.getData().setScale(3f);

        try {
            playerTex = new Texture("player.png");
        } catch (Exception e) {
            playerTex = solid(32, 32, Color.WHITE);
        }

        baseTex = solid((int) baseW, (int) baseH, new Color(0.2f, 0.5f, 1f, 1f));

        platforms.add(new Platform(340, GROUND_Y + 50,  300, 8));
        platforms.add(new Platform(440, GROUND_Y + 120, 200, 8));

        laneY = new float[] {
            GROUND_Y,
            platforms.get(0).top(),
            platforms.get(1).top()
        };

        stage = new Stage(new ScreenViewport());
        skin  = new Skin();

        skin.add("up",   solid(150, 40, new Color(0.18f, 0.18f, 0.18f, 1f)));
        skin.add("down", solid(150, 40, new Color(0.12f, 0.12f, 0.12f, 1f)));
        skin.add("over", solid(150, 40, new Color(0.24f, 0.24f, 0.24f, 1f)));
        skin.add("font", font);

        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up        = skin.newDrawable("up");
        tbs.down      = skin.newDrawable("down");
        tbs.over      = skin.newDrawable("over");
        tbs.font      = font;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        Pixmap epm = new Pixmap((int) enemyW, (int) enemyH, Pixmap.Format.RGBA8888);
        epm.setColor(1f, 0.2f, 0.2f, 1f);
        epm.fill();
        enemyTex = new Texture(epm);
        epm.dispose();

        initSystems();
        Gdx.input.setInputProcessor(stage);
    }

    private void initSystems() {
        damageTextSystem = new DamageTextSystem();
        turretSystem     = new TurretSystem();
        particleSystem   = new ParticleSystem();
        projectileSystem = new ProjectileSystem();

        enemySystem = new EnemySystem(
            enemyTex, enemyW, enemyH,
            enemySpeed, enemyContactDamage, enemySpawnInterval
        );
        enemySystem.startFirstWave();

        shopSystem = new ShopSystem(stage, skin, new ShopSystem.ShopCallback() {
            @Override public void onTurretPurchased() {
                // Place turret just right of the base
                float tx = baseX + baseW + 10 + (shopSystem.getTurretsOwned() - 1) * (TurretSystem.Turret.W + 8);
                turretSystem.placeTurret(tx, baseY);
            }
            @Override public void onRangedAttackPurchased() {}
            @Override public void onFasterAttackPurchased() {
                attackCooldown = 0.15f;
            }
            @Override public void onStrongerAttackPurchased() {
                attackDamage = 15;
            }
            @Override public void onRepairBase() {
                int healed = baseHpMax - baseHp;
                baseHp = baseHpMax;
                damageTextSystem.add(baseX + baseW / 2f, baseY + baseH + 18f, healed);
            }
            @Override public int getBaseHp()    { return baseHp; }
            @Override public int getBaseHpMax() { return baseHpMax; }
        });
    }

    private void resetGame() {
        // Reset player
        px = 160; py = 64;
        vx = 0;   vy = 0;
        onGround = true;
        facingRight = true;
        attackActive = false;
        attackTimer = 0f;
        attackCooldown = 0.30f;
        attackCooldownTimer = 0f;
        attackDamage = 10;

        // Reset base
        baseHp = baseHpMax;

        // Dispose and re-create systems
        if (turretSystem     != null) turretSystem.dispose();
        if (enemySystem      != null) enemySystem.dispose();
        if (projectileSystem != null) projectileSystem.dispose();
        if (particleSystem   != null) particleSystem.dispose();

        // Remove old shop window from stage before creating a new one
        stage.clear();
        Gdx.input.setInputProcessor(stage);

        gameOver = false;
        initSystems();
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

        if (gameOver) {
            renderGameOver();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            shopSystem.toggle();
        }

        if (!shopSystem.isOpen()) {
            handleInput(dt);
        }

        damageTextSystem.update(dt);
        projectileSystem.update(dt, Gdx.graphics.getWidth(), GROUND_Y, platforms);
        particleSystem.update(dt);
        turretSystem.update(dt, enemySystem.getEnemies(), enemyW, enemyH,
            damageTextSystem, particleSystem, shopSystem, enemySystem,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        enemySystem.updateAndSpawn(dt, laneY);

        int baseDelta = enemySystem.updateEnemies(
            dt, GRAVITY, GROUND_Y, platforms,
            baseX, baseY, baseW, baseH,
            attackBounds, attackActive,
            damageTextSystem, particleSystem,
            attackDamage, shopSystem
        );

        projectileSystem.checkCollisions(
            enemySystem.getEnemies(),
            enemyW, enemyH,
            damageTextSystem, particleSystem,
            attackDamage, shopSystem,
            enemySystem
        );

        if (baseDelta != 0) changeBaseHp(baseDelta);

        if (baseHp <= 0) {
            gameOver = true;
            return;
        }

        stage.act(dt);

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        batch.draw(baseTex,   baseX, baseY, baseW, baseH);
        batch.draw(playerTex, px,    py);
        projectileSystem.draw(batch);
        particleSystem.draw(batch);
        drawHUD();
        damageTextSystem.draw(batch, font);
        batch.end();

        enemySystem.draw(batch, shapes);
        turretSystem.draw(batch, shapes);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float barWidth  = 64f;
        float barHeight = 8f;
        float barX      = baseX - 8;
        float barY      = baseY + baseH + 6;
        float pct       = (float) baseHp / baseHpMax;

        shapes.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapes.rect(barX, barY, barWidth, barHeight);
        shapes.setColor(0.2f, 0.85f, 0.3f, 1f);
        shapes.rect(barX, barY, barWidth * pct, barHeight);

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

    private void renderGameOver() {
        ScreenUtils.clear(0.05f, 0f, 0f, 1);

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        // Dark red overlay panel
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.15f, 0f, 0f, 1f);
        shapes.rect(screenW / 2f - 200, screenH / 2f - 100, 400, 200);
        shapes.end();

        batch.begin();

        // GAME OVER
        bigFont.setColor(1f, 0.15f, 0.15f, 1f);
        layout.setText(bigFont, "GAME OVER");
        bigFont.draw(batch, layout,
            screenW / 2f - layout.width / 2f,
            screenH / 2f + layout.height + 20f);

        // Prompt
        font.setColor(Color.WHITE);
        layout.setText(font, "Press ENTER to try again");
        font.draw(batch, layout,
            screenW / 2f - layout.width / 2f,
            screenH / 2f - 20f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetGame();
        }
    }

    private void handleInput(float dt) {
        boolean left      = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right     = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump      = Gdx.input.isKeyJustPressed(Input.Keys.UP);
        boolean down      = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean attackKey = Gdx.input.isKeyJustPressed(Input.Keys.J);
        boolean shootKey  = Gdx.input.isKeyJustPressed(Input.Keys.K);

        if (down && onGround && py > GROUND_Y) {
            dropThroughPlatform = true;
            py -= 5f;
            onGround = false;
        } else if (!down) {
            dropThroughPlatform = false;
        }

        if (left ^ right) {
            float dir = right ? 1f : -1f;
            vx += dir * MOVE_ACCEL * dt;
            facingRight = right;
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

        float screenW = Gdx.graphics.getWidth();
        if (px + PLAYER_W < 0) px = screenW;
        else if (px > screenW)  px = -PLAYER_W;

        onGround = false;

        if (py <= GROUND_Y) {
            py = GROUND_Y;
            vy = 0;
            onGround = true;
        }

        if (vy <= 0f) {
            for (Platform p : platforms) {
                if (dropThroughPlatform) continue;
                float platTop = p.top();
                boolean wasAbove = prevPy >= platTop;
                boolean nowBelow = py     <= platTop;
                boolean overlapX = (px + PLAYER_W) > p.x && px < (p.x + p.w);
                if (wasAbove && nowBelow && overlapX) {
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

        if (attackKey && attackCooldownTimer <= 0f) {
            attackActive = true;
            attackTimer  = attackDuration;
            attackCooldownTimer = attackCooldown;
        }

        if (shootKey && attackCooldownTimer <= 0f && shopSystem.hasRangedAttack()) {
            float shootX = facingRight ? (px + PLAYER_W) : px;
            float shootY = py + PLAYER_H / 2f;
            projectileSystem.shoot(shootX, shootY, facingRight, shopSystem.hasPierceUpgrade());
            attackCooldownTimer = attackCooldown;
        }

        float attackY = py + (PLAYER_H - ATTACK_H) / 2f;
        float attackX = facingRight ? (px + PLAYER_W) : (px - ATTACK_W);
        attackBounds.set(attackX, attackY, ATTACK_W, ATTACK_H);
    }

    private void drawHUD() {
        String waveText;
        if (enemySystem.isInPrep()) {
            waveText = "Wave " + (enemySystem.getWave() + 1) + " in "
                + (int) Math.ceil(enemySystem.getPrepTimer()) + "s";
        } else {
            waveText = "Wave " + enemySystem.getWave()
                + " | Alive: "         + enemySystem.getAliveCount()
                + " | Left to spawn: " + enemySystem.getRemainingToSpawn();
        }

        int screenH = Gdx.graphics.getHeight();
        font.setColor(Color.WHITE);
        font.draw(batch, waveText, 12, screenH - 12);
        font.draw(batch, "LEFT/RIGHT Move | UP Jump | J Melee | K Shoot | S Shop", 12, screenH - 32);

        if (shopSystem.hasRangedAttack()) {
            String shotType = shopSystem.hasPierceUpgrade() ? "PIERCE" : "NORMAL";
            font.draw(batch, "Shot: " + shotType, 12, screenH - 52);
        }
    }

    private void changeBaseHp(int delta) {
        if (delta == 0) return;
        baseHp = MathUtils.clamp(baseHp + delta, 0, baseHpMax);
        damageTextSystem.add(baseX + baseW / 2f, baseY + baseH + 18f, delta);
    }

    private Texture solid(int w, int h, Color c) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (batch            != null) batch.dispose();
        if (shapes           != null) shapes.dispose();
        if (playerTex        != null) playerTex.dispose();
        if (baseTex          != null) baseTex.dispose();
        if (enemyTex         != null) enemyTex.dispose();
        if (stage            != null) stage.dispose();
        if (skin             != null) skin.dispose();
        if (font             != null) font.dispose();
        if (bigFont          != null) bigFont.dispose();
        if (projectileSystem != null) projectileSystem.dispose();
        if (particleSystem   != null) particleSystem.dispose();
        if (turretSystem     != null) turretSystem.dispose();
        if (enemySystem      != null) enemySystem.dispose();
    }
}
