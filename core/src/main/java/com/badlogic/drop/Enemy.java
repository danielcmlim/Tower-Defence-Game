package com.badlogic.drop;

public class Enemy {
    public float x, y;
    public float vy;

    public int hp;
    public int maxHp;
    public boolean hasShield;

    public Enemy(int hp, boolean hasShield) {
        this.hp = hp;
        this.maxHp = hp;
        this.hasShield = hasShield;
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
