package com.bloxcraft.events;

import com.bloxcraft.data.BossType;
import com.bloxcraft.data.Fruit;
import com.bloxcraft.data.Island;
import com.bloxcraft.data.IslandData;
import com.bloxcraft.data.PlayerStats;
import com.bloxcraft.entity.BossEntity;
import com.bloxcraft.logic.IslandManager;
import com.bloxcraft.logic.Progression;
import com.bloxcraft.net.Sync;
import com.bloxcraft.registry.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

/** /bf stats | islands  — для всех;  /bf level|beli|eat|boss|island|reset — для операторов. */
public final class BfCommands {
    private BfCommands() {}

    private static boolean op(CommandSourceStack s) { return s.hasPermission(2); }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bf")
                .then(Commands.literal("stats").executes(c -> stats(c.getSource().getPlayerOrException())))
                .then(Commands.literal("islands").executes(c -> islands(c.getSource())))

                .then(Commands.literal("level").requires(BfCommands::op)
                        .then(Commands.argument("n", IntegerArgumentType.integer(1, PlayerStats.MAX_LEVEL))
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    PlayerStats s = PlayerStats.get(p);
                                    s.level = IntegerArgumentType.getInteger(c, "n");
                                    s.xp = 0;
                                    Progression.applyAttributes(p);
                                    Sync.sync(p);
                                    return 1;
                                })))
                .then(Commands.literal("beli").requires(BfCommands::op)
                        .then(Commands.argument("n", LongArgumentType.longArg(0))
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    Progression.addBeli(p, LongArgumentType.getLong(c, "n"));
                                    return 1;
                                })))
                .then(Commands.literal("eat").requires(BfCommands::op)
                        .then(Commands.argument("fruit", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Fruit.values()).map(f -> f.id), b))
                                .executes(c -> {
                                    Fruit f = Fruit.byId(StringArgumentType.getString(c, "fruit"));
                                    if (f == null) return fail(c.getSource(), "Нет такого фрукта");
                                    Progression.eat(c.getSource().getPlayerOrException(), f);
                                    return 1;
                                })))
                .then(Commands.literal("boss").requires(BfCommands::op)
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(BossType.values()).map(t -> t.id), b))
                                .executes(c -> {
                                    BossType t = BossType.byId(StringArgumentType.getString(c, "type"));
                                    if (t == null) return fail(c.getSource(), "Нет такого босса");
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    ServerLevel l = (ServerLevel) p.level();
                                    BossEntity b = ModEntities.BOSS.get().create(l);
                                    b.moveTo(p.getX() + 3, p.getY(), p.getZ(), 0, 0);
                                    b.setType(t);
                                    b.setPersistenceRequired();
                                    l.addFreshEntity(b);
                                    return 1;
                                })))
                .then(Commands.literal("island").requires(BfCommands::op)
                        .then(Commands.literal("build")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests(BfCommands::suggestIslands)
                                        .executes(c -> {
                                            Island i = Island.byId(StringArgumentType.getString(c, "id"));
                                            if (i == null) return fail(c.getSource(), "Нет такого острова");
                                            ServerPlayer p = c.getSource().getPlayerOrException();
                                            BlockPos center = p.blockPosition().below();
                                            IslandData data = IslandData.get(p.server.overworld());
                                            data.centers.put(i.id, center);
                                            data.built.remove(i.id);
                                            data.setDirty();
                                            IslandManager.enqueue(i, center);
                                            c.getSource().sendSuccess(() -> Component.literal(
                                                    "§aСтроим «" + i.name + "» здесь. Подождите несколько секунд."), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("tp")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests(BfCommands::suggestIslands)
                                        .executes(c -> {
                                            Island i = Island.byId(StringArgumentType.getString(c, "id"));
                                            ServerPlayer p = c.getSource().getPlayerOrException();
                                            BlockPos pos = i == null ? null : IslandData.get(p.server.overworld()).centers.get(i.id);
                                            if (pos == null) return fail(c.getSource(), "Остров не найден");
                                            p.teleportTo(pos.getX() + 0.5, pos.getY() + i.hill + 3, pos.getZ() + 6.5);
                                            return 1;
                                        }))))
                .then(Commands.literal("reset").requires(BfCommands::op)
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            PlayerStats s = PlayerStats.get(p);
                            s.level = 1; s.xp = 0; s.beli = 0; s.fruit = ""; s.mastery = 0;
                            s.energy = 100; s.quest = ""; s.questProgress = 0;
                            Progression.applyAttributes(p);
                            Sync.sync(p);
                            return 1;
                        })));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestIslands(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> c,
            com.mojang.brigadier.suggestion.SuggestionsBuilder b) {
        return SharedSuggestionProvider.suggest(Arrays.stream(Island.values()).map(i -> i.id), b);
    }

    private static int fail(CommandSourceStack s, String msg) {
        s.sendFailure(Component.literal(msg));
        return 0;
    }

    private static int stats(ServerPlayer p) {
        PlayerStats s = PlayerStats.get(p);
        Fruit f = Fruit.byId(s.fruit);
        p.sendSystemMessage(Component.literal("§6Уровень §f" + s.level + " §7(" + s.xp + "/" + PlayerStats.xpFor(s.level)
                + ")  §6Белли §f" + s.beli + "  §6Фрукт §f" + (f == null ? "нет" : f.nameRu)
                + "  §6Мастерство §f" + s.mastery));
        return 1;
    }

    private static int islands(CommandSourceStack src) {
        IslandData d = IslandData.get(src.getServer().overworld());
        for (Island i : Island.values()) {
            BlockPos c = d.centers.get(i.id);
            src.sendSuccess(() -> Component.literal("§e" + i.name + " §7[" + i.id + "] §f"
                    + (c == null ? "—" : c.getX() + ", " + c.getY() + ", " + c.getZ())), false);
        }
        return 1;
    }
}
