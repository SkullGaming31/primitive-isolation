package com.canadiendragon.primitiveisolation;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.gamerules.GameRules;

public final class SleepingMatSleep {
    private static final long DAY_LENGTH = 24_000L;
    private static final Set<ServerLevel> ACCELERATED_LEVELS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<ServerLevel, Long> SLEEP_TARGET_TICKS = new IdentityHashMap<>();

    private SleepingMatSleep() {
    }

    public static boolean shouldAccelerate(ServerLevel level, ServerClockManager clockManager) {
        if (!level.getGameRules().get(GameRules.ADVANCE_TIME)
                || !hasSleepingMatPlayer(level)
                || !hasEnoughPlayersSleeping(level)) {
            SLEEP_TARGET_TICKS.remove(level);
            return false;
        }

        Holder<WorldClock> clock = level.dimensionType().defaultClock().orElse(null);
        if (clock == null) {
            SLEEP_TARGET_TICKS.remove(level);
            return false;
        }

        SLEEP_TARGET_TICKS.computeIfAbsent(level, ignored -> {
            long totalTicks = clockManager.getTotalTicks(clock);
            return totalTicks + DAY_LENGTH - Math.floorMod(totalTicks, DAY_LENGTH);
        });
        return true;
    }

    public static boolean shouldWakeAtMorning(ServerLevel level, ServerClockManager clockManager) {
        Long targetTicks = SLEEP_TARGET_TICKS.get(level);
        if (targetTicks == null) {
            return false;
        }

        Holder<WorldClock> clock = level.dimensionType().defaultClock().orElse(null);
        if (clock == null) {
            SLEEP_TARGET_TICKS.remove(level);
            return false;
        }
        if (clockManager.getTotalTicks(clock) < targetTicks) {
            return false;
        }

        SLEEP_TARGET_TICKS.remove(level);
        return true;
    }

    public static boolean hasSleepingMatPlayer(ServerLevel level) {
        return level.players().stream().anyMatch(player -> player.isSleeping()
                && player.getSleepingPos()
                        .map(pos -> level.getBlockState(pos).is(primitiveisolation.SLEEPING_MAT.get()))
                        .orElse(false));
    }

    public static Set<ServerLevel> getAcceleratedLevels() {
        return Set.copyOf(ACCELERATED_LEVELS);
    }

    public static void setAcceleratedLevels(Set<ServerLevel> levels) {
        ACCELERATED_LEVELS.clear();
        ACCELERATED_LEVELS.addAll(levels);
    }

    public static void clearSleepTargets() {
        SLEEP_TARGET_TICKS.clear();
    }

    private static boolean hasEnoughPlayersSleeping(ServerLevel level) {
        int activePlayers = 0;
        int sleepingPlayers = 0;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }

            activePlayers++;
            if (player.isSleeping()) {
                sleepingPlayers++;
            }
        }

        int requiredSleepers = Math.max(
                1,
                (int) Math.ceil(activePlayers * level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE) / 100.0));
        return sleepingPlayers >= requiredSleepers;
    }
}
