package com.bloxcraft.events;

import com.bloxcraft.BloxCraft;
import com.bloxcraft.Config;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.data.Quest;
import com.bloxcraft.entity.BossEntity;
import com.bloxcraft.entity.QuestNpcEntity;
import com.bloxcraft.item.BloxSwordItem;
import com.bloxcraft.logic.Fx;
import com.bloxcraft.logic.IslandManager;
import com.bloxcraft.logic.Progression;
import com.bloxcraft.logic.Scheduler;
import com.bloxcraft.net.Sync;
import com.bloxcraft.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = BloxCraft.MODID)
public final class GameEvents {
    private GameEvents() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        BfCommands.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent e) {
        IslandManager.init(e.getServer());
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent e) {
        Scheduler.clear();
        IslandManager.clear();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            Progression.applyAttributes(sp);
            Sync.sync(sp);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            Progression.applyAttributes(sp);
            PlayerStats.get(sp).energy = PlayerStats.get(sp).maxEnergy();
            Sync.sync(sp);
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post e) {
        Scheduler.tick();
        IslandManager.tick(e.getServer());
        if (e.getServer().getTickCount() % 20 == 0) {
            for (ServerPlayer p : e.getServer().getPlayerList().getPlayers()) {
                PlayerStats s = PlayerStats.get(p);
                s.energy = Math.min(s.maxEnergy(), s.energy + 3 + s.maxEnergy() * 0.02);
                Sync.sync(p);
            }
        }
    }

    /** Пассивки фруктов, бонус урона от уровня, требования к уровню меча. */
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent e) {
        LivingEntity victim = e.getEntity();
        DamageSource src = e.getSource();

        if (victim instanceof ServerPlayer vp) {
            Fruit f = Fruit.byId(PlayerStats.get(vp).fruit);
            if (f != null) {
                if ((f == Fruit.FLAME || f == Fruit.MAGMA) && src.is(DamageTypeTags.IS_FIRE)) { e.setCanceled(true); return; }
                if (f == Fruit.ICE && src.is(DamageTypeTags.IS_FREEZING)) { e.setCanceled(true); return; }
                if ((f == Fruit.RUBBER || f == Fruit.DRAGON) && src.is(DamageTypeTags.IS_FALL)) { e.setCanceled(true); return; }
                if (f == Fruit.BUDDHA) e.setAmount(e.getAmount() * 0.8f);
            }
        }

        if (src.getEntity() instanceof ServerPlayer atk && !Fx.inAbility) {
            PlayerStats s = PlayerStats.get(atk);
            if (atk.getMainHandItem().getItem() instanceof BloxSwordItem sw && s.level < sw.type.minLevel) {
                atk.displayClientMessage(Component.literal("§cМеч требует уровень " + sw.type.minLevel), true);
                e.setCanceled(true);
                return;
            }
            e.setAmount(e.getAmount() * (1f + s.level * 0.01f));
        }
    }

    /** Опыт, белли, квесты и редкий дроп фруктов. */
    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.level().isClientSide) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer p)) return;
        if (dead instanceof Player || dead instanceof QuestNpcEntity) return;

        long xp, beli;
        if (dead instanceof BossEntity b) {
            xp = b.type().xp;
            beli = b.type().beli;
        } else {
            xp = Math.max(1, (long) (dead.getMaxHealth() * 2));
            beli = Math.max(1, xp / 2);
        }
        Progression.addBeli(p, beli);
        Progression.addXp(p, xp);

        PlayerStats s = PlayerStats.get(p);
        Quest q = Quest.byId(s.quest);
        if (q != null && s.questProgress < q.count && q.matches(dead)) {
            s.questProgress++;
            if (s.questProgress >= q.count) {
                p.sendSystemMessage(Component.literal("§a✔ Цель задания «" + q.title + "» выполнена! Вернитесь к заказчику."));
            } else {
                p.displayClientMessage(Component.literal("§e" + q.title + ": " + s.questProgress + "/" + q.count), true);
            }
            Sync.sync(p);
        }

        if (dead instanceof Monster && !(dead instanceof BossEntity)
                && p.getRandom().nextDouble() < Config.FRUIT_DROP_CHANCE.get()) {
            dead.spawnAtLocation(new ItemStack(ModItems.fruit(Fruit.random(p.getRandom(), 1))));
        }
    }
}
