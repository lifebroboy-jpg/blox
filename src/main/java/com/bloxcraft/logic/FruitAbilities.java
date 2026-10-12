package com.bloxcraft.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Реализация способностей всех дьявольских фруктов. pw — множитель силы (растёт с уровнем). */
public final class FruitAbilities {
    private FruitAbilities() {}

    private static final ParticleOptions SAND =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());

    // ───────────── ПЕСОК ─────────────
    public static void sandBlast(ServerPlayer p, double pw) {
        Fx.bolt(p, 12, 1.2, 5 * pw, SAND, 0, 0.8);
    }
    public static void sandTornado(ServerPlayer p, double pw) {
        Fx.boom(p, p.position(), 6, 6 * pw, 0, 1.1, SAND);
        for (LivingEntity e : Fx.radius(p, p.position(), 6)) Fx.effect(e, MobEffects.BLINDNESS, 60, 0);
    }
    public static void sandSpear(ServerPlayer p, double pw) {
        Fx.bolt(p, 18, 0.7, 8 * pw, SAND, 0, 0.6);
    }

    // ───────────── РЕЗИНА ─────────────
    public static void gumPistol(ServerPlayer p, double pw) {
        Fx.bolt(p, 12, 0.8, 5 * pw, ParticleTypes.CRIT, 0, 1.6);
    }
    public static void gumBarrage(ServerPlayer p, double pw) {
        for (int i = 0; i < 6; i++) Scheduler.later(i * 3, () -> Fx.bolt(p, 6, 1.0, 2.5 * pw, ParticleTypes.CRIT, 0, 0.25));
    }
    public static void gumRocket(ServerPlayer p, double pw) {
        Fx.dash(p, 2.2, 0.1);
        Scheduler.later(5, () -> Fx.boom(p, p.position(), 3.5, 7 * pw, 0, 1.4, ParticleTypes.CLOUD));
    }

    // ───────────── ПЛАМЯ ─────────────
    public static void flameFist(ServerPlayer p, double pw) {
        Fx.bolt(p, 14, 1.0, 6 * pw, ParticleTypes.FLAME, 5, 0.4);
        Fx.sound(p, SoundEvents.BLAZE_SHOOT);
    }
    public static void flamePillar(ServerPlayer p, double pw) {
        Vec3 c = Fx.aim(p, 20);
        Fx.column(p, c, 8, ParticleTypes.FLAME);
        Fx.boom(p, c, 4.5, 8 * pw, 6, 0.8, ParticleTypes.FLAME);
    }
    public static void flameDash(ServerPlayer p, double pw) {
        Fx.dash(p, 1.8, 0.1);
        Scheduler.later(4, () -> Fx.boom(p, p.position(), 3.5, 5 * pw, 5, 0.8, ParticleTypes.FLAME));
    }

    // ───────────── ЛЁД ─────────────
    public static void iceShards(ServerPlayer p, double pw) {
        for (LivingEntity e : Fx.bolt(p, 14, 1.2, 5 * pw, ParticleTypes.SNOWFLAKE, 0, 0.3)) {
            Fx.effect(e, MobEffects.MOVEMENT_SLOWDOWN, 80, 2);
            e.setTicksFrozen(200);
        }
    }
    public static void iceWall(ServerPlayer p, double pw) {
        ServerLevel l = Fx.lvl(p);
        Direction f = p.getDirection();
        Direction side = f.getClockWise();
        BlockPos base = p.blockPosition().relative(f, 3);
        List<BlockPos> placed = new ArrayList<>();
        for (int w = -2; w <= 2; w++) {
            for (int h = 0; h <= 2; h++) {
                BlockPos bp = base.relative(side, w).above(h);
                if (l.getBlockState(bp).isAir()) {
                    l.setBlock(bp, Blocks.BLUE_ICE.defaultBlockState(), 3);
                    placed.add(bp);
                }
            }
        }
        Scheduler.later(200, () -> {
            for (BlockPos bp : placed) if (l.getBlockState(bp).is(Blocks.BLUE_ICE)) l.removeBlock(bp, false);
        });
    }
    public static void iceFreeze(ServerPlayer p, double pw) {
        for (LivingEntity e : Fx.boom(p, p.position(), 8, 4 * pw, 0, 0.3, ParticleTypes.SNOWFLAKE)) {
            Fx.effect(e, MobEffects.MOVEMENT_SLOWDOWN, 120, 4);
            e.setTicksFrozen(300);
        }
    }

    // ───────────── СВЕТ ─────────────
    public static void lightBeam(ServerPlayer p, double pw) {
        Fx.bolt(p, 30, 0.8, 7 * pw, ParticleTypes.END_ROD, 0, 0.3);
    }
    public static void lightSpeed(ServerPlayer p, double pw) {
        Fx.dash(p, 3.2, 0.05);
        Fx.effect(p, MobEffects.MOVEMENT_SPEED, 100, 2);
        Fx.lvl(p).sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
    }
    public static void lightFlash(ServerPlayer p, double pw) {
        for (LivingEntity e : Fx.boom(p, p.position(), 10, 5 * pw, 0, 0.5, ParticleTypes.END_ROD)) {
            Fx.effect(e, MobEffects.BLINDNESS, 100, 0);
        }
    }

    // ───────────── МАГМА ─────────────
    public static void magmaFist(ServerPlayer p, double pw) {
        Fx.bolt(p, 8, 1.4, 8 * pw, ParticleTypes.LAVA, 6, 0.5);
    }
    public static void magmaMeteors(ServerPlayer p, double pw) {
        Vec3 c = Fx.aim(p, 25);
        ServerLevel l = Fx.lvl(p);
        for (int i = 0; i < 6; i++) {
            Vec3 m = c.add((p.getRandom().nextDouble() - 0.5) * 8, 0, (p.getRandom().nextDouble() - 0.5) * 8);
            Scheduler.later(i * 8, () -> {
                for (double y = 18; y >= 0; y -= 2) {
                    l.sendParticles(ParticleTypes.FLAME, m.x, m.y + y, m.z, 4, 0.2, 0.2, 0.2, 0.01);
                    l.sendParticles(ParticleTypes.LARGE_SMOKE, m.x, m.y + y, m.z, 1, 0.2, 0.2, 0.2, 0.01);
                }
                Scheduler.later(10, () -> Fx.boom(p, m, 3.2, 6 * pw, 6, 0.7, ParticleTypes.LAVA));
            });
        }
    }
    public static void magmaEruption(ServerPlayer p, double pw) {
        Fx.boom(p, p.position(), 7, 7 * pw, 8, 1.0, ParticleTypes.LAVA);
        Fx.column(p, p.position(), 6, ParticleTypes.LAVA);
    }

    // ───────────── ЗЕМЛЕТРЯСЕНИЕ ─────────────
    public static void quakeShock(ServerPlayer p, double pw) {
        Fx.boom(p, p.position(), 6, 5 * pw, 0, 1.2, ParticleTypes.CLOUD);
    }
    public static void quakePunch(ServerPlayer p, double pw) {
        Fx.bolt(p, 6, 1.5, 10 * pw, ParticleTypes.EXPLOSION, 0, 2.5);
    }
    public static void quakeQuake(ServerPlayer p, double pw) {
        Fx.boom(p, p.position(), 12, 6 * pw, 0, 1.4, ParticleTypes.CLOUD);
        Scheduler.later(20, () -> Fx.boom(p, p.position(), 12, 6 * pw, 0, 1.4, ParticleTypes.CLOUD));
    }

    // ───────────── БУДДА ─────────────
    public static void buddhaPalm(ServerPlayer p, double pw) {
        Fx.bolt(p, 7, 1.6, 9 * pw, ParticleTypes.POOF, 0, 2.0);
    }
    public static void buddhaForm(ServerPlayer p, double pw) {
        Fx.effect(p, MobEffects.DAMAGE_RESISTANCE, 300, 2);
        Fx.effect(p, MobEffects.ABSORPTION, 300, 3);
        Fx.effect(p, MobEffects.REGENERATION, 300, 1);
        Fx.lvl(p).sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 0.6, 0.9, 0.6, 0.05);
    }
    public static void buddhaWave(ServerPlayer p, double pw) {
        Fx.boom(p, p.position(), 8, 6 * pw, 0, 2.0, ParticleTypes.POOF);
    }

    // ───────────── ТЬМА ─────────────
    public static void darkHole(ServerPlayer p, double pw) {
        Vec3 c = Fx.aim(p, 18);
        ServerLevel l = Fx.lvl(p);
        for (int i = 0; i < 20; i++) {
            Scheduler.later(i * 2, () -> {
                l.sendParticles(ParticleTypes.PORTAL, c.x, c.y + 1, c.z, 25, 1.5, 1.5, 1.5, 0.3);
                l.sendParticles(ParticleTypes.SQUID_INK, c.x, c.y + 1, c.z, 6, 0.6, 0.6, 0.6, 0.02);
                for (LivingEntity e : Fx.radius(p, c, 9)) {
                    Vec3 dir = c.subtract(e.position()).normalize().scale(0.35);
                    e.setDeltaMovement(e.getDeltaMovement().add(dir));
                    e.hurtMarked = true;
                }
            });
        }
        Scheduler.later(42, () -> Fx.boom(p, c, 5, 7 * pw, 0, 0.5, ParticleTypes.SQUID_INK));
    }
    public static void darkVortex(ServerPlayer p, double pw) {
        for (LivingEntity e : Fx.boom(p, p.position(), 7, 4 * pw, 0, 0.6, ParticleTypes.SQUID_INK)) {
            Fx.effect(e, MobEffects.BLINDNESS, 100, 0);
            Fx.effect(e, MobEffects.WITHER, 100, 1);
        }
    }
    public static void darkStep(ServerPlayer p, double pw) {
        ServerLevel l = Fx.lvl(p);
        l.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        HitResult hr = p.pick(12, 0.0f, false);
        Vec3 t = hr.getLocation();
        if (hr.getType() == HitResult.Type.BLOCK) t = t.subtract(p.getLookAngle().scale(0.8));
        p.teleportTo(t.x, t.y - p.getEyeHeight(), t.z);
        p.fallDistance = 0;
        l.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        Fx.sound(p, SoundEvents.ENDERMAN_TELEPORT);
    }

    // ───────────── ДРАКОН ─────────────
    public static void dragonBreath(ServerPlayer p, double pw) {
        for (int i = 0; i < 3; i++) {
            Scheduler.later(i * 4, () -> Fx.bolt(p, 12, 2.0, 3 * pw, ParticleTypes.DRAGON_BREATH, 6, 0.2));
        }
    }
    public static void dragonFly(ServerPlayer p, double pw) {
        Fx.dash(p, 1.6, 0.9);
        Fx.effect(p, MobEffects.SLOW_FALLING, 120, 0);
    }
    public static void dragonClaw(ServerPlayer p, double pw) {
        for (LivingEntity e : Fx.boom(p, p.position(), 5, 9 * pw, 0, 1.0, ParticleTypes.SWEEP_ATTACK)) {
            Fx.effect(e, MobEffects.WEAKNESS, 100, 1);
        }
    }
}
