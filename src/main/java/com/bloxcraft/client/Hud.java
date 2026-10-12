package com.bloxcraft.client;

import com.bloxcraft.data.Ability;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.Quest;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

public final class Hud {
    private Hud() {}

    public static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        CompoundTag t = ClientStats.tag;
        if (!t.contains("level")) return;

        Font f = mc.font;
        int x = 8, y = 8, w = 130;
        Fruit fruit = Fruit.byId(t.getString("fruit"));
        Quest q = Quest.byId(t.getString("quest"));
        int rows = 4 + (fruit != null ? 3 : 0) + (q != null ? 1 : 0);
        g.fill(x - 4, y - 4, x + w + 4, y + rows * 11 + 4, 0x90000000);

        g.drawString(f, "Ур. " + t.getInt("level") + "   §6" + t.getLong("beli") + " белли", x, y, 0xFFFFFF);
        y += 11;

        long xp = t.getLong("xp"), need = Math.max(1, t.getLong("need"));
        g.fill(x, y, x + w, y + 5, 0xFF333333);
        g.fill(x, y, x + (int) (w * Math.min(1.0, (double) xp / need)), y + 5, 0xFF55FF55);
        y += 8;

        double en = t.getDouble("energy"), max = Math.max(1, t.getDouble("max"));
        g.fill(x, y, x + w, y + 5, 0xFF333333);
        g.fill(x, y, x + (int) (w * Math.min(1.0, en / max)), y + 5, 0xFF3C8CFF);
        y += 8;

        g.drawString(f, "Фрукт: " + (fruit == null ? "§7нет" : "§b" + fruit.nameRu + " §7(маст. " + t.getInt("mastery") + ")"), x, y, 0xFFFFFF);
        y += 11;

        if (fruit != null) {
            for (int i = 0; i < fruit.abilities.length; i++) {
                Ability a = fruit.abilities[i];
                String key = ClientModEvents.KEYS[i].getTranslatedKeyMessage().getString();
                int cd = t.getInt("cd" + i);
                String state;
                int col;
                if (t.getInt("mastery") < a.minMastery()) { state = " §8(маст. " + a.minMastery() + ")"; col = 0x777777; }
                else if (cd > 0) { state = String.format(" §c%.1fс", cd / 20.0); col = 0xAAAAAA; }
                else { state = ""; col = 0xFFFFFF; }
                g.drawString(f, "[" + key + "] " + a.name() + state, x, y, col);
                y += 11;
            }
        }
        if (q != null) {
            g.drawString(f, "§eКвест: §f" + q.title + " " + t.getInt("qp") + "/" + q.count, x, y, 0xFFFFFF);
        }
    }
}
