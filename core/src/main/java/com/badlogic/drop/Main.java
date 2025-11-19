package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.math.Rectangle;


public class Main implements ApplicationListener {

    private SpriteBatch batch;
    private ShapeRenderer shapes;

    private Texture playerTex;
    private float px = 160, py = 64;   // position
    private float vx = 0,   vy = 0;    // velocity
    private boolean onGround = true;

    // Tunables
    private static final float GROUND_Y   = 64f;
    private static final float MOVE_ACCEL = 900f;
    private static final float MOVE_MAX   = 220f;
    private static final float FRICTION   = 1400f;
    private static final float GRAVITY    = -900f;
    private static final float JUMP_VY    = 380f;

    // --- Base placeholder + HP ---
    private Texture baseTex;
    private float baseX = 40, baseY = GROUND_Y; // left side “base”
    private float baseW = 48, baseH = 32;

    private int baseHp = 100;
    private int baseHpMax = 100;

    private Stage stage;
    private Skin skin;
    private BitmapFont font;

    private Texture enemyTex;
    private float ex, ey;
    private float enemyW = 28, enemyH = 28;
    private float enemySpeed = 60f;
    private int enemyContactDamage = 10;
    private boolean enemyAlive = false;

    private float enemyRespawnTimer = 1.5f;
    private float enemyRespawnDelay = 2.0f;

    // --- Player attack (melee) ---
    private boolean attackActive = false;
    private float attackTimer = 0f;
    private float attackDuration = 0.15f;      // how long hitbox is active
    private float attackCooldown = 0.30f;      // time between swings
    private float attackCooldownTimer = 0f;

    private float attackOffset = 24f;          // how far in front of player

    private boolean facingRight = true;

    private static final float PLAYER_W = 32f;
    private static final float PLAYER_H = 32f;

    private static final float ATTACK_W = 20f;
    private static final float ATTACK_H = 24f;

    private final Rectangle attackBounds = new Rectangle();


    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        // player setup (white box substitute since there is no sprite)
        try {
            playerTex = new Texture("player.png");
        } catch (Exception e) {
            playerTex = solid(32, 32, Color.WHITE);
        }

        // base setup
        baseTex = solid((int)baseW, (int)baseH, new Color(0.2f, 0.5f, 1f, 1f));

        // Minimal UI skin built in code (no external assets)
        stage = new Stage(new ScreenViewport());
        font = new BitmapFont();
        skin = new Skin();

        // button setup
        Texture upTex   = solid(150, 40, new Color(0.18f, 0.18f, 0.18f, 1f));
        Texture downTex = solid(150, 40, new Color(0.12f, 0.12f, 0.12f, 1f));
        Texture overTex = solid(150, 40, new Color(0.24f, 0.24f, 0.24f, 1f));
        skin.add("up",   upTex);
        skin.add("down", downTex);
        skin.add("over", overTex);
        skin.add("font", font);

        //enemy setup
        Pixmap epm = new Pixmap((int)enemyW, (int)enemyH, Pixmap.Format.RGBA8888);
        epm.setColor(1f, 0.2f, 0.2f, 1f);
        epm.fill();
        enemyTex = new Texture(epm);
        epm.dispose();


        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up   = skin.newDrawable("up");
        tbs.down = skin.newDrawable("down");
        tbs.over = skin.newDrawable("over");
        tbs.font = font;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        TextButton damageBtn = new TextButton("Damage -10", skin);
        TextButton healBtn   = new TextButton("Heal +10", skin);

        damageBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                changeBaseHp(-10);
            }
        });
        healBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                changeBaseHp(+10);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.bottom().right().pad(12);
        root.add(damageBtn).pad(6);
        root.add(healBtn).pad(6);
        stage.addActor(root);

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
        baseHp = MathUtils.clamp(baseHp + delta, 0, baseHpMax);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }


    private void spawnEnemy() {
        float screenW = Gdx.graphics.getWidth();
        ex = screenW - enemyW - 10;
        ey = GROUND_Y;
        enemyAlive = true;
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump  = Gdx.input.isKeyJustPressed(Input.Keys.UP);
        boolean attackKey = Gdx.input.isKeyJustPressed(Input.Keys.J);

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

        vy += GRAVITY * dt;
        px += vx * dt;
        py += vy * dt;

        if (py <= GROUND_Y) {
            py = GROUND_Y;
            vy = 0;
            onGround = true;
        }

        // --- Attack timers ---
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= dt;
        }
        if (attackTimer > 0f) {
            attackTimer -= dt;
            if (attackTimer <= 0f) {
                attackActive = false;
            }
        }

        if (attackKey && attackCooldownTimer <= 0f) {
            attackActive = true;
            attackTimer = attackDuration;
            attackCooldownTimer = attackCooldown;
        }

        // Center the hitbox vertically on the player
        float attackY = py + (PLAYER_H - ATTACK_H) / 2f;
        float attackX;

        if (facingRight) {
            // Completely to the right of the player
            attackX = px + PLAYER_W;
        } else {
            // Completely to the left of the player
            attackX = px - ATTACK_W;
        }

        attackBounds.set(attackX, attackY, ATTACK_W, ATTACK_H);





        if (!enemyAlive) {
            enemyRespawnTimer -= dt;
            if (enemyRespawnTimer <= 0f) {
                spawnEnemy();
            }
        } else {
            ex -= enemySpeed * dt;

            // Check if attack hit enemy
            if (attackActive && enemyAlive) {
                boolean hitEnemy =
                    attackBounds.x < ex + enemyW &&
                        attackBounds.x + ATTACK_W > ex &&
                        attackBounds.y < ey + enemyH &&
                        attackBounds.y + ATTACK_H > ey;

                if (hitEnemy) {
                    enemyAlive = false;
                    enemyRespawnTimer = enemyRespawnDelay;
                }
            }



            boolean touchesBase =
                ex <= (baseX + baseW) &&
                    (ey < baseY + baseH) && ((ey + enemyH) > baseY);

            if (touchesBase) {
                baseHp = MathUtils.clamp(baseHp - enemyContactDamage, 0, baseHpMax);
                enemyAlive = false;
                enemyRespawnTimer = enemyRespawnDelay;
            }
        }


        stage.act(dt);

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        batch.draw(baseTex, baseX, baseY, baseW, baseH);
        batch.draw(playerTex, px, py);

        if (enemyAlive) {
            batch.draw(enemyTex, ex, ey, enemyW, enemyH);
        }

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
        // foreground (green→red as it shrinks could be added later; for now green)
        shapes.setColor(0.2f, 0.85f, 0.3f, 1f);
        shapes.rect(barX, barY, fgW, barHeight);
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

    }
}
