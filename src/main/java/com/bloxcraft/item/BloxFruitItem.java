package com.bloxcraft.item;

import com.bloxcraft.data.Ability;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.logic.Progression;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BloxFruitItem extends Item {
    public final Fruit fruit;
    private static final String[] KEYS = {"Z", "X", "C"};

    public BloxFruitItem(Fruit fruit, Properties props) {
        super(props);
        this.fruit = fruit;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("§7Присядьте и нажмите ПКМ, чтобы съесть фрукт"), true);
            }
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerStats s = PlayerStats.get(sp);
            if (fruit.id.equals(s.fruit)) {
                sp.displayClientMessage(Component.literal("§cВы уже владеете этим фруктом"), true);
                return InteractionResultHolder.fail(stack);
            }
            Progression.eat(sp, fruit);
            stack.shrink(1);
            level.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1f, 0.7f);
            sp.sendSystemMessage(Component.literal("§d🍎 Вы съели §b" + fruit.nameRu
                    + "§d! Способности: клавиши Z / X / C."));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7Редкость: §f" + fruit.tier() + " §7| Цена: §6" + fruit.price));
        for (int i = 0; i < fruit.abilities.length; i++) {
            Ability a = fruit.abilities[i];
            tip.add(Component.literal("§9[" + KEYS[i] + "] §f" + a.name() + " §8(" + a.cost() + " эн., маст. " + a.minMastery() + ")"));
        }
        tip.add(Component.literal("§8Присесть + ПКМ — съесть"));
    }
}
