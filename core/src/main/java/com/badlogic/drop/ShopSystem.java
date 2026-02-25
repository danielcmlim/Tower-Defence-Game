package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ShopSystem {

    private final Stage stage;
    private Table shopWindow;
    private boolean isOpen = false;

    private int coins = 20;
    private Label coinsLabel;
    private TextButton repairBtn;

    private boolean hasRangedAttack   = false;
    private boolean hasFasterAttack   = false;
    private boolean hasStrongerAttack = false;
    private boolean hasPierceUpgrade  = false;
    private int turretsOwned = 0;

    private static final int   RANGED_ATTACK_PRICE   = 30;
    private static final int   FASTER_ATTACK_PRICE   = 20;
    private static final int   STRONGER_ATTACK_PRICE = 25;
    private static final int   PIERCE_PRICE          = 35;
    private static final int   TURRET_PRICE          = 50;
    private static final float REPAIR_COST_PER_HP    = 0.5f;

    private final ShopCallback callback;

    public interface ShopCallback {
        void onRangedAttackPurchased();
        void onFasterAttackPurchased();
        void onStrongerAttackPurchased();
        void onRepairBase();
        int getBaseHp();
        int getBaseHpMax();
        void onTurretPurchased();
    }

    public ShopSystem(Stage stage, Skin skin, ShopCallback callback) {
        this.stage    = stage;
        this.callback = callback;
        createShopUI(skin);
    }

    private void createShopUI(Skin skin) {
        shopWindow = new Table();
        shopWindow.setBackground(skin.newDrawable("up", new Color(0.1f, 0.1f, 0.15f, 0.95f)));
        shopWindow.pad(20);

        Label titleLabel = new Label("SHOP", new Label.LabelStyle(skin.getFont("font"), Color.YELLOW));
        shopWindow.add(titleLabel).colspan(2).padBottom(15).row();

        coinsLabel = new Label("Coins: " + coins, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        shopWindow.add(coinsLabel).colspan(2).padBottom(20).row();

        Label repairLabel = new Label("Repair Base (Full Heal)", new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        repairBtn = new TextButton("Buy: 0", skin);
        repairBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { purchaseRepair(); }
        });
        shopWindow.add(repairLabel).left().padRight(10);
        shopWindow.add(repairBtn).width(140).row();

        addUpgradeRow(skin, "Ranged Attack  (shoot with K)", "Buy: " + RANGED_ATTACK_PRICE,
            new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) { purchaseRangedAttack(); }
            });

        addUpgradeRow(skin, "Faster Attack  (shorter cooldown)", "Buy: " + FASTER_ATTACK_PRICE,
            new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) { purchaseFasterAttack(); }
            });

        addUpgradeRow(skin, "Stronger Attack  (+5 damage)", "Buy: " + STRONGER_ATTACK_PRICE,
            new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) { purchaseStrongerAttack(); }
            });

        addUpgradeRow(skin, "Pierce Shot  (passes through enemies)", "Buy: " + PIERCE_PRICE,
            new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) { purchasePierce(); }
            });

        addUpgradeRow(skin, "Turret  (place a shooting turret)", "Buy: " + TURRET_PRICE,
            new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) { purchaseTurret(); }
            });

        TextButton closeBtn = new TextButton("Close (ESC)", skin);
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { toggle(); }
        });
        shopWindow.add(closeBtn).colspan(2).padTop(20).width(200).row();

        shopWindow.pack();
        shopWindow.setPosition(
            (Gdx.graphics.getWidth()  - shopWindow.getWidth())  / 2f,
            (Gdx.graphics.getHeight() - shopWindow.getHeight()) / 2f
        );

        shopWindow.setVisible(false);
        stage.addActor(shopWindow);
    }

    private void addUpgradeRow(Skin skin, String labelText, String btnText, ClickListener listener) {
        Label label = new Label(labelText, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        TextButton btn = new TextButton(btnText, skin);
        btn.addListener(listener);
        shopWindow.add(label).left().padRight(10).padTop(10);
        shopWindow.add(btn).width(140).padTop(10).row();
    }

    private void purchaseRepair() {
        if (callback == null) return;
        int hpNeeded = callback.getBaseHpMax() - callback.getBaseHp();
        if (hpNeeded <= 0) return;
        int cost = (int) Math.ceil(hpNeeded * REPAIR_COST_PER_HP);
        if (trySpend(cost)) callback.onRepairBase();
    }

    private void purchaseRangedAttack() {
        if (hasRangedAttack) return;
        if (trySpend(RANGED_ATTACK_PRICE)) {
            hasRangedAttack = true;
            if (callback != null) callback.onRangedAttackPurchased();
        }
    }

    private void purchaseFasterAttack() {
        if (hasFasterAttack) return;
        if (trySpend(FASTER_ATTACK_PRICE)) {
            hasFasterAttack = true;
            if (callback != null) callback.onFasterAttackPurchased();
        }
    }

    private void purchaseStrongerAttack() {
        if (hasStrongerAttack) return;
        if (trySpend(STRONGER_ATTACK_PRICE)) {
            hasStrongerAttack = true;
            if (callback != null) callback.onStrongerAttackPurchased();
        }
    }

    private void purchasePierce() {
        if (hasPierceUpgrade) return;
        if (trySpend(PIERCE_PRICE)) hasPierceUpgrade = true;
    }

    private void purchaseTurret() {
        if (trySpend(TURRET_PRICE)) {
            turretsOwned++;
            if (callback != null) callback.onTurretPurchased();
        }
    }

    private boolean trySpend(int cost) {
        if (coins >= cost) {
            coins -= cost;
            updateCoinsLabel();
            return true;
        }
        return false;
    }

    private void updateCoinsLabel() {
        coinsLabel.setText("Coins: " + coins);
    }

    public void updateRepairButton() {
        if (callback == null) return;
        int hpNeeded = callback.getBaseHpMax() - callback.getBaseHp();
        if (hpNeeded <= 0) {
            repairBtn.setText("Full HP");
        } else {
            int cost = (int) Math.ceil(hpNeeded * REPAIR_COST_PER_HP);
            repairBtn.setText("Buy: " + cost);
        }
    }

    public void toggle() {
        isOpen = !isOpen;
        shopWindow.setVisible(isOpen);
        if (isOpen) updateRepairButton();
    }

    public boolean isOpen()            { return isOpen; }

    public void addCoins(int amount) {
        coins += amount;
        updateCoinsLabel();
    }

    public boolean hasRangedAttack()   { return hasRangedAttack; }
    public boolean hasFasterAttack()   { return hasFasterAttack; }
    public boolean hasStrongerAttack() { return hasStrongerAttack; }
    public boolean hasPierceUpgrade()  { return hasPierceUpgrade; }
    public int getTurretsOwned()       { return turretsOwned; }
}
