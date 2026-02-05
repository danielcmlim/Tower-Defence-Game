package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ShopSystem {

    private Stage stage;
    private Table shopWindow;
    private boolean isOpen = false;

    private int coins = 50; // Starting money
    private Label coinsLabel;
    private TextButton repairBtn;
    private Label repairLabel;

    // Upgrade states
    private boolean hasRangedAttack = false;
    private boolean hasFasterAttack = false;
    private boolean hasStrongerAttack = false;

    // Upgrade prices
    private static final int RANGED_ATTACK_PRICE = 30;
    private static final int FASTER_ATTACK_PRICE = 20;
    private static final int STRONGER_ATTACK_PRICE = 25;
    private static final float REPAIR_COST_PER_HP = 0.5f; // 0.5 coins per HP

    private ShopCallback callback;

    public interface ShopCallback {
        void onRangedAttackPurchased();
        void onFasterAttackPurchased();
        void onStrongerAttackPurchased();
        void onRepairBase();
        int getBaseHp();
        int getBaseHpMax();
    }

    public ShopSystem(Stage stage, Skin skin, ShopCallback callback) {
        this.stage = stage;
        this.callback = callback;
        createShopUI(skin);
    }

    private void createShopUI(Skin skin) {
        shopWindow = new Table();
        shopWindow.setBackground(skin.newDrawable("up", new Color(0.1f, 0.1f, 0.15f, 0.95f)));
        shopWindow.pad(20);

        // Title
        Label titleLabel = new Label("SHOP", new Label.LabelStyle(skin.getFont("font"), Color.YELLOW));
        shopWindow.add(titleLabel).colspan(2).padBottom(15).row();

        // Coins display
        coinsLabel = new Label("Coins: " + coins, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        shopWindow.add(coinsLabel).colspan(2).padBottom(20).row();

        // Base Repair
        repairLabel = new Label("Repair Base (Full Heal)",
            new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        repairBtn = new TextButton("Buy: 0", skin);
        repairBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                purchaseRepair();
            }
        });
        shopWindow.add(repairLabel).left().padRight(10);
        shopWindow.add(repairBtn).width(120).row();

        // Ranged Attack upgrade
        Label rangedLabel = new Label("Ranged Attack (Shoot projectiles)",
            new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        TextButton rangedBtn = new TextButton("Buy: " + RANGED_ATTACK_PRICE, skin);
        rangedBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                purchaseRangedAttack();
            }
        });
        shopWindow.add(rangedLabel).left().padRight(10).padTop(10);
        shopWindow.add(rangedBtn).width(120).padTop(10).row();

        // Faster Attack upgrade
        Label fasterLabel = new Label("Faster Attack (Reduced cooldown)",
            new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        TextButton fasterBtn = new TextButton("Buy: " + FASTER_ATTACK_PRICE, skin);
        fasterBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                purchaseFasterAttack();
            }
        });
        shopWindow.add(fasterLabel).left().padRight(10).padTop(10);
        shopWindow.add(fasterBtn).width(120).padTop(10).row();

        // Stronger Attack upgrade
        Label strongerLabel = new Label("Stronger Attack (+5 damage)",
            new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        TextButton strongerBtn = new TextButton("Buy: " + STRONGER_ATTACK_PRICE, skin);
        strongerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                purchaseStrongerAttack();
            }
        });
        shopWindow.add(strongerLabel).left().padRight(10).padTop(10);
        shopWindow.add(strongerBtn).width(120).padTop(10).row();

        // Close button
        TextButton closeBtn = new TextButton("Close (ESC)", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle();
            }
        });
        shopWindow.add(closeBtn).colspan(2).padTop(20).width(200).row();

        // Position in center of screen
        shopWindow.pack();
        shopWindow.setPosition(
            (Gdx.graphics.getWidth() - shopWindow.getWidth()) / 2,
            (Gdx.graphics.getHeight() - shopWindow.getHeight()) / 2
        );

        shopWindow.setVisible(false);
        stage.addActor(shopWindow);
    }

    private void purchaseRepair() {
        if (callback == null) return;

        int currentHp = callback.getBaseHp();
        int maxHp = callback.getBaseHpMax();
        int hpNeeded = maxHp - currentHp;

        if (hpNeeded <= 0) {
            System.out.println("Base already at full health!");
            return;
        }

        int cost = (int)Math.ceil(hpNeeded * REPAIR_COST_PER_HP);

        if (coins >= cost) {
            coins -= cost;
            updateCoinsLabel();
            callback.onRepairBase();
            System.out.println("Base fully repaired for " + cost + " coins!");
        } else {
            System.out.println("Not enough coins! Need " + cost + " coins.");
        }
    }

    private void purchaseRangedAttack() {
        if (hasRangedAttack) {
            System.out.println("Already purchased!");
            return;
        }
        if (coins >= RANGED_ATTACK_PRICE) {
            coins -= RANGED_ATTACK_PRICE;
            hasRangedAttack = true;
            updateCoinsLabel();
            if (callback != null) callback.onRangedAttackPurchased();
            System.out.println("Ranged Attack purchased!");
        } else {
            System.out.println("Not enough coins!");
        }
    }

    private void purchaseFasterAttack() {
        if (hasFasterAttack) {
            System.out.println("Already purchased!");
            return;
        }
        if (coins >= FASTER_ATTACK_PRICE) {
            coins -= FASTER_ATTACK_PRICE;
            hasFasterAttack = true;
            updateCoinsLabel();
            if (callback != null) callback.onFasterAttackPurchased();
            System.out.println("Faster Attack purchased!");
        } else {
            System.out.println("Not enough coins!");
        }
    }

    private void purchaseStrongerAttack() {
        if (hasStrongerAttack) {
            System.out.println("Already purchased!");
            return;
        }
        if (coins >= STRONGER_ATTACK_PRICE) {
            coins -= STRONGER_ATTACK_PRICE;
            hasStrongerAttack = true;
            updateCoinsLabel();
            if (callback != null) callback.onStrongerAttackPurchased();
            System.out.println("Stronger Attack purchased!");
        } else {
            System.out.println("Not enough coins!");
        }
    }

    private void updateCoinsLabel() {
        coinsLabel.setText("Coins: " + coins);
    }

    public void updateRepairButton() {
        if (callback == null) return;

        int currentHp = callback.getBaseHp();
        int maxHp = callback.getBaseHpMax();
        int hpNeeded = maxHp - currentHp;

        if (hpNeeded <= 0) {
            repairBtn.setText("Full HP");
        } else {
            int cost = (int)Math.ceil(hpNeeded * REPAIR_COST_PER_HP);
            repairBtn.setText("Buy: " + cost);
        }
    }

    public void toggle() {
        isOpen = !isOpen;
        shopWindow.setVisible(isOpen);
        if (isOpen) {
            updateRepairButton();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void addCoins(int amount) {
        coins += amount;
        updateCoinsLabel();
    }

    public boolean hasRangedAttack() {
        return hasRangedAttack;
    }

    public boolean hasFasterAttack() {
        return hasFasterAttack;
    }

    public boolean hasStrongerAttack() {
        return hasStrongerAttack;
    }
}
