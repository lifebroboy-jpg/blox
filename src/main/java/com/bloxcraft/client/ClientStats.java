package com.bloxcraft.client;

import net.minecraft.nbt.CompoundTag;

/** Копия статов игрока на клиенте (приходит с сервера раз в секунду и при изменениях). */
public final class ClientStats {
    public static CompoundTag tag = new CompoundTag();

    public static void set(CompoundTag t) { tag = t; }

    private ClientStats() {}
}
