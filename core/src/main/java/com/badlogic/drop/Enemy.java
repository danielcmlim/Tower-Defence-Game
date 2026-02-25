package com.badlogic.drop;

public class Enemy {
    public enum Type { NORMAL, SNIPER, TANK }

    public float x, y;
    public float vy;

    public int hp;
    public int maxHp;
    public boolean hasShield;
    public boolean hitThisSwing = false;
    public Type type;

    // Sniper-specific
    public float shootCooldown = 0f;
    public static final float SNIPER_SHOOT_INTERVAL = 2.5f;

    public Enemy(int hp, boolean hasShield, Type type) {
        this.hp = hp;
        this.maxHp = hp;
        this.hasShield = hasShield;
        this.type = type;
        if (type == Type.SNIPER) {
            shootCooldown = SNIPER_SHOOT_INTERVAL;
        }
    }


    public boolean takeDamage(int amount) {
        if (hasShield) {
            hasShield = false;
            return false;
        }
        hp -= amount;
        return hp <= 0;
    }
}
