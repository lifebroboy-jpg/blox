package com.bloxcraft.logic;

import java.util.ArrayList;
import java.util.List;

/** Простой планировщик отложенных задач (серверный поток). */
public final class Scheduler {
    private record Task(long at, Runnable run) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0;

    public static void later(int delayTicks, Runnable r) {
        TASKS.add(new Task(now + Math.max(0, delayTicks), r));
    }

    public static void tick() {
        now++;
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        TASKS.removeIf(t -> {
            if (t.at <= now) { due.add(t); return true; }
            return false;
        });
        for (Task t : due) {
            try { t.run.run(); } catch (Exception ignored) {}
        }
    }

    public static void clear() { TASKS.clear(); }

    private Scheduler() {}
}
