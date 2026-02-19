package com.badlogic.drop;

public class DamageText {
    public float x, y;
    public String text;
    public float life;
    public float vy;

    public DamageText(float x, float y, String text, float life, float vy) {
        this.x    = x;
        this.y    = y;
        this.text = text;
        this.life = life;
        this.vy   = vy;
    }
}
