package com.badlogic.drop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class DamageTextSystem {

    private final Array<DamageText> texts = new Array<>();

    public void add(float x, float y, int amount) {
        String t = (amount >= 0 ? "+" : "") + amount;
        texts.add(new DamageText(x, y, t, 0.7f, 40f));
    }

    public void addText(float x, float y, String text) {
        texts.add(new DamageText(x, y, text, 0.9f, 35f));
    }

    public void update(float dt) {
        for (int i = texts.size - 1; i >= 0; i--) {
            DamageText d = texts.get(i);
            d.life -= dt;
            d.y    += d.vy * dt;
            if (d.life <= 0f) texts.removeIndex(i);
        }
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        for (DamageText d : texts) {
            float alpha = MathUtils.clamp(d.life / 0.7f, 0f, 1f);

            if (d.text.equals("BLOCKED"))  font.setColor(1f, 0.6f, 0.1f, alpha);
            else if (d.text.startsWith("-")) font.setColor(1f, 0.3f, 0.3f, alpha);
            else                             font.setColor(0.3f, 1f, 0.3f, alpha);

            font.draw(batch, d.text, d.x, d.y);
        }
        font.setColor(Color.WHITE);
    }
}
