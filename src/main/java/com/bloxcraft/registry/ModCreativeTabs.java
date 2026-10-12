package com.bloxcraft.registry;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.data.Fruit;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BloxCraft.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bloxcraft"))
                    .icon(() -> new ItemStack(ModItems.fruit(Fruit.FLAME)))
                    .displayItems((params, out) ->
                            ModItems.ITEMS.getEntries().forEach(h -> out.accept(h.get())))
                    .build());

    private ModCreativeTabs() {}
}
