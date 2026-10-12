package com.bloxcraft.data;

import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public enum BossType {
    BOBBY("bobby", "Бобби Красный Кулак", 400, 6, 1.2, 1, SwordType.CUTLASS, SwordType.KATANA,
            EntityType.ZOMBIE, 500, 400, BossEvent.BossBarColor.RED),
    GORILLA_KING("gorilla_king", "Король горилл", 800, 9, 1.5, 1, SwordType.BISENTO, SwordType.CUTLASS,
            EntityType.SPIDER, 1200, 900, BossEvent.BossBarColor.GREEN),
    SABER_EXPERT("saber_expert", "Мастер сабли", 1500, 13, 1.2, 2, SwordType.SHISUI, SwordType.BISENTO,
            EntityType.HUSK, 3500, 2500, BossEvent.BossBarColor.YELLOW),
    ICE_ADMIRAL("ice_admiral", "Ледяной адмирал", 2500, 17, 1.3, 2, SwordType.SADDI, SwordType.SHISUI,
            EntityType.STRAY, 8000, 6000, BossEvent.BossBarColor.BLUE),
    MAGMA_ADMIRAL("magma_admiral", "Магмовый адмирал", 4000, 22, 1.4, 3, SwordType.YAMA, SwordType.SADDI,
            EntityType.SKELETON, 20000, 12000, BossEvent.BossBarColor.RED),
    GREYBEARD("greybeard", "Седобородый", 7000, 28, 1.4, 3, SwordType.DARK_BLADE, SwordType.YAMA,
            EntityType.VINDICATOR, 50000, 30000, BossEvent.BossBarColor.WHITE),
    DARKBEARD("darkbeard", "Тёмная Борода", 12000, 40, 1.6, 3, SwordType.CURSED_DUAL_KATANA, SwordType.DARK_BLADE,
            EntityType.WITHER_SKELETON, 120000, 80000, BossEvent.BossBarColor.PURPLE);

    public final String id, display;
    public final double hp, attack, scale;
    public final int maxRarity;
    public final SwordType drop, weapon;
    public final EntityType<? extends Mob> minion;
    public final long xp, beli;
    public final BossEvent.BossBarColor color;

    BossType(String id, String display, double hp, double attack, double scale, int maxRarity,
             SwordType drop, SwordType weapon, EntityType<? extends Mob> minion,
             long xp, long beli, BossEvent.BossBarColor color) {
        this.id = id;
        this.display = display;
        this.hp = hp;
        this.attack = attack;
        this.scale = scale;
        this.maxRarity = maxRarity;
        this.drop = drop;
        this.weapon = weapon;
        this.minion = minion;
        this.xp = xp;
        this.beli = beli;
        this.color = color;
    }

    public static BossType byIndex(int i) {
        BossType[] v = values();
        return v[Math.floorMod(i, v.length)];
    }

    public static BossType byId(String id) {
        for (BossType t : values()) if (t.id.equals(id)) return t;
        return null;
    }
}
