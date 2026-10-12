package com.bloxcraft.logic;

import com.bloxcraft.entity.QuestNpcEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Общие помощники для способностей: урон, лучи, взрывы, рывки. */
public final class Fx {
    /** true, пока способность наносит урон (чтобы не умножать урон бонусом уровня дважды). */
    public static boolean inAbility = false;

    private Fx() {}

    public static ServerLevel lvl(ServerPlayer p) { return (ServerLevel) p.level(); }

    public static boolean valid(Entity e, ServerPlayer p) {
        return e instanceof LivingEntity le && le != p && le.isAlive()
                && !(e instanceof QuestNpcEntity) && !(e instanceof ArmorStand);
    }

    public static void hit(ServerPlayer p, LivingEntity t, double dmg) {
        inAbility = true;
        try {
            t.invulnerableTime = 0;
            t.hurt(p.damageSources().playerAttack(p), (float) dmg);
        } finally {
            inAbility = false;
        }
    }

    public static void push(LivingEntity t, Vec3 dir, double strength, double up) {
        Vec3 d = new Vec3(dir.x, 0, dir.z);
        d = d.lengthSqr() < 1.0E-6 ? Vec3.ZERO : d.normalize();
        t.setDeltaMovement(t.getDeltaMovement().add(d.x * strength, up, d.z * strength));
        t.hurtMarked = true;
    }

    public static void effect(LivingEntity e, Holder<MobEffect> eff, int ticks, int amp) {
        e.addEffect(new MobEffectInstance(eff, ticks, amp));
    }

    public static void sound(ServerPlayer p, SoundEvent s) {
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static Vec3 aim(ServerPlayer p, double range) {
        return p.pick(range, 0.0f, false).getLocation();
    }

    public static List<LivingEntity> radius(ServerPlayer p, Vec3 c, double r) {
        return lvl(p).getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r),
                e -> valid(e, p) && e.getBoundingBox().getCenter().distanceToSqr(c) <= r * r);
    }

    public static void column(ServerPlayer p, Vec3 c, double height, ParticleOptions part) {
        ServerLevel l = lvl(p);
        for (double y = 0; y <= height; y += 0.5) {
            l.sendParticles(part, c.x, c.y + y, c.z, 6, 0.5, 0.1, 0.5, 0.02);
        }
    }

    /** Луч перед игроком: рисует частицы и бьёт всех на линии. */
    public static List<LivingEntity> bolt(ServerPlayer p, double range, double width, double dmg,
                                          ParticleOptions part, int fireSec, double knock) {
        ServerLevel l = lvl(p);
        Vec3 s = p.getEyePosition();
        Vec3 d = p.getLookAngle();
        double len = range;
        HitResult hr = p.pick(range, 0.0f, false);
        if (hr.getType() == HitResult.Type.BLOCK) len = hr.getLocation().distanceTo(s);

        for (double t = 1; t <= len; t += 0.7) {
            Vec3 pt = s.add(d.scale(t));
            l.sendParticles(part, pt.x, pt.y - 0.2, pt.z, 3, 0.1, 0.1, 0.1, 0.01);
        }

        List<LivingEntity> out = new ArrayList<>();
        AABB box = new AABB(s, s.add(d.scale(len))).inflate(width + 1);
        for (LivingEntity e : l.getEntitiesOfClass(LivingEntity.class, box, x -> valid(x, p))) {
            Vec3 c = e.getBoundingBox().getCenter();
            double t = Mth.clamp(c.subtract(s).dot(d), 0, len);
            Vec3 closest = s.add(d.scale(t));
            if (c.distanceTo(closest) <= width + e.getBbWidth() / 2.0) {
                hit(p, e, dmg);
                if (fireSec > 0) e.setRemainingFireTicks(fireSec * 20);
                if (knock > 0) push(e, d, knock, 0.25);
                out.add(e);
            }
        }
        return out;
    }

    /** Взрыв-AoE без разрушения блоков. */
    public static List<LivingEntity> boom(ServerPlayer p, Vec3 c, double r, double dmg, int fireSec,
                                          double knock, ParticleOptions part) {
        ServerLevel l = lvl(p);
        l.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 1, 0, 0, 0, 0);
        l.sendParticles(part, c.x, c.y + 0.7, c.z, 60, r / 2.5, 0.6, r / 2.5, 0.08);
        l.playSound(null, c.x, c.y, c.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.8f, 1.2f);
        List<LivingEntity> hit = radius(p, c, r);
        for (LivingEntity e : hit) {
            hit(p, e, dmg);
            if (fireSec > 0) e.setRemainingFireTicks(fireSec * 20);
            if (knock > 0) push(e, e.position().subtract(c), knock, 0.35);
        }
        return hit;
    }

    public static void dash(ServerPlayer p, double speed, double up) {
        Vec3 d = p.getLookAngle();
        p.setDeltaMovement(d.x * speed, d.y * speed + up, d.z * speed);
        p.hurtMarked = true;
        p.fallDistance = 0;
    }
}
