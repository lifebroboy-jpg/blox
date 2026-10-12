package com.bloxcraft.registry;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.SwordType;
import com.bloxcraft.item.BloxFruitItem;
import com.bloxcraft.item.BloxSwordItem;
import com.bloxcraft.item.LogPoseItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BloxCraft.MODID);

    public static final Map<Fruit, Supplier<? extends Item>> FRUITS = new EnumMap<>(Fruit.class);
    public static final Map<SwordType, Supplier<? extends Item>> SWORDS = new EnumMap<>(SwordType.class);
    public static final Supplier<? extends Item> LOG_POSE;

    static {
        for (Fruit f : Fruit.values()) {
            FRUITS.put(f, ITEMS.register(f.id + "_fruit",
                    () -> new BloxFruitItem(f, new Item.Properties().stacksTo(1).rarity(f.rarity))));
        }
        for (SwordType t : SwordType.values()) {
            SWORDS.put(t, ITEMS.register(t.id, () -> new BloxSwordItem(t)));
        }
        LOG_POSE = ITEMS.register("log_pose", () -> new LogPoseItem(new Item.Properties().stacksTo(1)));
    }

    public static Item fruit(Fruit f) { return FRUITS.get(f).get(); }
    public static Item sword(SwordType t) { return SWORDS.get(t).get(); }

    private ModItems() {}
}
