package com.bloxcraft.item;

import com.bloxcraft.data.Island;
import com.bloxcraft.data.IslandData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

/** Лог-пос: ПКМ — направление на ближайший остров, Shift+ПКМ — список всех островов. */
public class LogPoseItem extends Item {
    public LogPoseItem(Properties props) { super(props); }

    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack st = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp && level.getServer() != null) {
            ServerLevel o = level.getServer().overworld();
            IslandData d = IslandData.get(o);
            if (sp.isShiftKeyDown()) {
                sp.sendSystemMessage(Component.literal("§6— Острова —"));
                for (Island i : Island.values()) {
                    BlockPos c = d.centers.get(i.id);
                    if (c == null) continue;
                    sp.sendSystemMessage(Component.literal("§e" + i.name + " §7(" + (d.built.contains(i.id) ? "готов" : "строится")
                            + ") §f" + c.getX() + ", " + c.getY() + ", " + c.getZ()));
                }
            } else {
                Island best = null;
                BlockPos bc = null;
                double bd = Double.MAX_VALUE;
                for (Map.Entry<String, BlockPos> e : d.centers.entrySet()) {
                    Island i = Island.byId(e.getKey());
                    if (i == null) continue;
                    double dist = Math.sqrt(sp.blockPosition().distSqr(e.getValue()));
                    if (dist < bd) { bd = dist; best = i; bc = e.getValue(); }
                }
                if (best == null) {
                    sp.displayClientMessage(Component.literal("§cОстровов пока нет"), true);
                } else {
                    double dx = bc.getX() - sp.getX(), dz = bc.getZ() - sp.getZ();
                    double target = Math.toDegrees(Math.atan2(-dx, dz));
                    double rel = Mth.wrapDegrees(target - sp.getYRot());
                    int idx = Math.floorMod((int) Math.round(rel / 45.0), 8);
                    sp.displayClientMessage(Component.literal("§6" + ARROWS[idx] + " §e" + best.name
                            + " §7— §f" + (int) bd + " блоков"), true);
                }
            }
            sp.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(st, level.isClientSide());
    }
}
