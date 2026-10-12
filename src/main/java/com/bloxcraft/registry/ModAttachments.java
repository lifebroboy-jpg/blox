package com.bloxcraft.registry;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.data.PlayerStats;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BloxCraft.MODID);

    public static final Supplier<AttachmentType<PlayerStats>> STATS = ATTACHMENTS.register("stats",
            () -> AttachmentType.serializable(PlayerStats::new).copyOnDeath().build());

    private ModAttachments() {}
}
