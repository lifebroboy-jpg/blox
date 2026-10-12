package com.bloxcraft;

import com.bloxcraft.registry.ModAttachments;
import com.bloxcraft.registry.ModCreativeTabs;
import com.bloxcraft.registry.ModEntities;
import com.bloxcraft.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(BloxCraft.MODID)
public class BloxCraft {
    public static final String MODID = "bloxcraft";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public BloxCraft(IEventBus modBus, ModContainer container) {
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
