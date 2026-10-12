package com.bloxcraft.data;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.bloxcraft.entity.BossEntity;

public enum Quest {
    BANDITS("bandits", "Бандиты", 1, EntityType.ZOMBIE, null, "зомби-бандитов", 6, 300, 200),
    MONKEYS("monkeys", "Дикие обезьяны", 10, EntityType.SPIDER, null, "пауков-обезьян", 8, 1500, 800),
    DESERT_BANDITS("desert_bandits", "Пустынные бандиты", 40, EntityType.HUSK, null, "кадавров пустыни", 10, 7000, 2500),
    SNOW_TROOPERS("snow_troopers", "Снежные солдаты", 90, EntityType.STRAY, null, "ледяных стрелков", 10, 20000, 6000),
    LAVA_BOMBERS("lava_bombers", "Подрывники магмы", 150, EntityType.CREEPER, null, "подрывников", 12, 45000, 12000),
    GREYBEARD_HUNT("greybeard_hunt", "Охота на Седобородого", 250, null, BossType.GREYBEARD, "Седобородого", 1, 80000, 25000),
    SKY_ENDERS("sky_enders", "Гости из бездны", 400, EntityType.ENDERMAN, null, "эндерменов", 10, 120000, 50000);

    public final String id, title, targetRu;
    public final int minLevel, count;
    public final EntityType<?> target;
    public final BossType boss;
    public final long xp, beli;

    Quest(String id, String title, int minLevel, EntityType<?> target, BossType boss,
          String targetRu, int count, long xp, long beli) {
        this.id = id;
        this.title = title;
        this.minLevel = minLevel;
        this.target = target;
        this.boss = boss;
        this.targetRu = targetRu;
        this.count = count;
        this.xp = xp;
        this.beli = beli;
    }

    public boolean matches(LivingEntity dead) {
        if (boss != null) return dead instanceof BossEntity b && b.type() == boss;
        return dead.getType() == target;
    }

    public static Quest byId(String id) {
        if (id == null || id.isEmpty()) return null;
        for (Quest q : values()) if (q.id.equals(id)) return q;
        return null;
    }
}
