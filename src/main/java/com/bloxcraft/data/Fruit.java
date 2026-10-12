package com.bloxcraft.data;

import com.bloxcraft.logic.FruitAbilities;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.List;

public enum Fruit {
    SAND("sand", "Sand Fruit", "Фрукт Песка", Rarity.COMMON, 800,
            new Ability("Песчаный взрыв", 20, 30, 0, FruitAbilities::sandBlast),
            new Ability("Песчаный торнадо", 30, 90, 25, FruitAbilities::sandTornado),
            new Ability("Копьё пустыни", 35, 70, 75, FruitAbilities::sandSpear)),
    RUBBER("rubber", "Rubber Fruit", "Резиновый фрукт", Rarity.COMMON, 800,
            new Ability("Резиновый пистолет", 15, 25, 0, FruitAbilities::gumPistol),
            new Ability("Резиновая очередь", 30, 80, 25, FruitAbilities::gumBarrage),
            new Ability("Резиновая ракета", 25, 70, 75, FruitAbilities::gumRocket)),
    FLAME("flame", "Flame Fruit", "Фрукт Пламени", Rarity.UNCOMMON, 1500,
            new Ability("Огненный кулак", 20, 30, 0, FruitAbilities::flameFist),
            new Ability("Огненный столп", 30, 80, 25, FruitAbilities::flamePillar),
            new Ability("Огненный рывок", 25, 60, 75, FruitAbilities::flameDash)),
    ICE("ice", "Ice Fruit", "Фрукт Льда", Rarity.UNCOMMON, 1500,
            new Ability("Ледяные шипы", 20, 30, 0, FruitAbilities::iceShards),
            new Ability("Ледяная стена", 30, 100, 25, FruitAbilities::iceWall),
            new Ability("Заморозка", 40, 120, 75, FruitAbilities::iceFreeze)),
    LIGHT("light", "Light Fruit", "Фрукт Света", Rarity.RARE, 3500,
            new Ability("Луч света", 25, 35, 0, FruitAbilities::lightBeam),
            new Ability("Скорость света", 25, 60, 25, FruitAbilities::lightSpeed),
            new Ability("Вспышка", 40, 120, 75, FruitAbilities::lightFlash)),
    MAGMA("magma", "Magma Fruit", "Фрукт Магмы", Rarity.RARE, 3500,
            new Ability("Магмовый кулак", 25, 35, 0, FruitAbilities::magmaFist),
            new Ability("Метеоритный дождь", 50, 180, 25, FruitAbilities::magmaMeteors),
            new Ability("Извержение", 40, 120, 75, FruitAbilities::magmaEruption)),
    QUAKE("quake", "Quake Fruit", "Фрукт Землетрясения", Rarity.RARE, 4000,
            new Ability("Ударная волна", 25, 40, 0, FruitAbilities::quakeShock),
            new Ability("Удар землетрясения", 35, 70, 25, FruitAbilities::quakePunch),
            new Ability("Землетрясение", 60, 200, 75, FruitAbilities::quakeQuake)),
    BUDDHA("buddha", "Buddha Fruit", "Фрукт Будды", Rarity.EPIC, 7500,
            new Ability("Гигантская ладонь", 30, 45, 0, FruitAbilities::buddhaPalm),
            new Ability("Форма Будды", 45, 400, 25, FruitAbilities::buddhaForm),
            new Ability("Волна Будды", 50, 140, 75, FruitAbilities::buddhaWave)),
    DARK("dark", "Dark Fruit", "Фрукт Тьмы", Rarity.EPIC, 7500,
            new Ability("Чёрная дыра", 45, 150, 0, FruitAbilities::darkHole),
            new Ability("Вихрь тьмы", 35, 90, 25, FruitAbilities::darkVortex),
            new Ability("Шаг тени", 20, 40, 75, FruitAbilities::darkStep)),
    DRAGON("dragon", "Dragon Fruit", "Фрукт Дракона", Rarity.EPIC, 15000,
            new Ability("Дыхание дракона", 40, 80, 0, FruitAbilities::dragonBreath),
            new Ability("Полёт дракона", 30, 100, 25, FruitAbilities::dragonFly),
            new Ability("Коготь дракона", 50, 120, 75, FruitAbilities::dragonClaw));

    public final String id;
    public final String nameEn;
    public final String nameRu;
    public final Rarity rarity;
    public final long price;
    public final Ability[] abilities;

    Fruit(String id, String nameEn, String nameRu, Rarity rarity, long price, Ability... abilities) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameRu = nameRu;
        this.rarity = rarity;
        this.price = price;
        this.abilities = abilities;
    }

    public String tier() {
        return switch (rarity) {
            case COMMON -> "Обычный";
            case UNCOMMON -> "Необычный";
            case RARE -> "Редкий";
            default -> "Легендарный";
        };
    }

    public static Fruit byId(String id) {
        if (id == null || id.isEmpty()) return null;
        for (Fruit f : values()) if (f.id.equals(id)) return f;
        return null;
    }

    /** Случайный фрукт с редкостью не выше maxRarity (0 = COMMON ... 3 = EPIC). */
    public static Fruit random(RandomSource r, int maxRarity) {
        List<Fruit> pool = new ArrayList<>();
        for (Fruit f : values()) if (f.rarity.ordinal() <= maxRarity) pool.add(f);
        return pool.get(r.nextInt(pool.size()));
    }
}
