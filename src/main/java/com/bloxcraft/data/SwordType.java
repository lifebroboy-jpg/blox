package com.bloxcraft.data;

import com.bloxcraft.item.BloxTier;

public enum SwordType {
    KATANA("katana", "Katana", "Катана", 2, -2.2f, 1200, 0, 1000, Effect.NONE, 4),
    CUTLASS("cutlass", "Cutlass", "Абордажная сабля", 3, -2.2f, 1500, 10, 2500, Effect.NONE, 5),
    BISENTO("bisento", "Bisento", "Бисенто", 5, -2.6f, 1800, 40, 6000, Effect.SLOW, 7),
    SHISUI("shisui", "Shisui", "Шисуи", 6, -2.0f, 2200, 90, 12000, Effect.FROST, 9),
    SADDI("saddi", "Saddi", "Сэдди", 7, -2.0f, 2500, 150, 25000, Effect.LIGHTNING, 12),
    YAMA("yama", "Yama", "Яма", 9, -2.2f, 3000, 250, 45000, Effect.FIRE, 16),
    DARK_BLADE("dark_blade", "Dark Blade (Yoru)", "Тёмный клинок (Ёру)", 12, -2.0f, 3500, 400, 90000, Effect.WITHER, 22),
    CURSED_DUAL_KATANA("cursed_dual_katana", "Cursed Dual Katana", "Проклятые двойные катаны", 15, -1.8f, 5000, 550, 0, Effect.LIFESTEAL, 30);

    public enum Effect {
        NONE("нет"), SLOW("замедление"), FROST("заморозка"), LIGHTNING("молния"),
        FIRE("поджог"), WITHER("иссушение"), LIFESTEAL("кража здоровья");
        public final String ru;
        Effect(String ru) { this.ru = ru; }
    }

    public final String id, nameEn, nameRu;
    public final float speed;
    public final int minLevel;
    /** 0 = не продаётся (только с боссов). */
    public final long price;
    public final Effect effect;
    public final double slash;
    public final BloxTier tier;

    SwordType(String id, String en, String ru, float bonus, float speed, int uses,
              int minLevel, long price, Effect effect, double slash) {
        this.id = id;
        this.nameEn = en;
        this.nameRu = ru;
        this.speed = speed;
        this.minLevel = minLevel;
        this.price = price;
        this.effect = effect;
        this.slash = slash;
        this.tier = new BloxTier(uses, 8.0f, bonus, 15);
    }

    public static SwordType byId(String id) {
        for (SwordType t : values()) if (t.id.equals(id)) return t;
        return null;
    }
}
