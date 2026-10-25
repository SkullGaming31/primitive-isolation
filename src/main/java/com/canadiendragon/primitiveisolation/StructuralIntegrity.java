package com.canadiendragon.primitiveisolation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = primitiveisolation.MODID)
public final class StructuralIntegrity {
    private static final int MAX_COMPONENT_BLOCKS = 8192;
    private static final int MAX_HORIZONTAL_COMPONENT_RADIUS = 32;
    private static final int MAX_VERTICAL_COMPONENT_RADIUS = 128;
    private static final int MAX_FALLS_PER_TICK = 16;
    private static final double MAX_SEARCH_DISTANCE = 14.0;
    private static final double DEFAULT_STRENGTH = 4.0;
    private static final double WOOD_STRENGTH = 5.0;
    private static final double BEAM_STRENGTH = 12.0;
    private static final double VERTICAL_SUPPORT_COST = 0.25;
    private static final double POST_SUPPORT_COST = 0.05;
    private static final double BEAM_SUPPORT_COST = 0.5;

    private static final Map<ServerLevel, Set<BlockPos>> DIRTY_POSITIONS = new IdentityHashMap<>();
    private static final Map<ServerLevel, LinkedHashSet<BlockPos>> PENDING_FALLS = new IdentityHashMap<>();

    private StructuralIntegrity() {}

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            markDirty(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BreakBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            markDirty(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onNeighborNotified(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            markDirty(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            processDirtyPositions(level);
            processPendingFalls(level);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            DIRTY_POSITIONS.remove(level);
            PENDING_FALLS.remove(level);
        }
    }

    private static void markDirty(ServerLevel level, BlockPos pos) {
        if (!Config.STRUCTURAL_INTEGRITY_ENABLED.getAsBoolean()) {
            return;
        }

        Set<BlockPos> dirty = DIRTY_POSITIONS.computeIfAbsent(level, ignored -> new LinkedHashSet<>());
        dirty.add(pos.immutable());
        for (Direction direction : Direction.values()) {
            dirty.add(pos.relative(direction));
        }
    }

    private static void processDirtyPositions(ServerLevel level) {
        Set<BlockPos> dirty = DIRTY_POSITIONS.remove(level);
        if (dirty == null) {
            return;
        }

        Set<BlockPos> checked = new HashSet<>();
        for (BlockPos pos : dirty) {
            if (checked.contains(pos) || !level.hasChunkAt(pos)) {
                continue;
            }

            Component component = discoverComponent(level, pos);
            if (component.overflowed()) {
                primitiveisolation.LOGGER.warn(
                        "Skipped structural integrity check near {} in {} because the connected structure exceeds {} blocks",
                        pos,
                        level.dimension(),
                        MAX_COMPONENT_BLOCKS);
                continue;
            }
            checked.addAll(component.blocks().keySet());
            checked.addAll(component.anchors());
            if (!component.blocks().isEmpty()) {
                queueUnsupportedBlocks(level, component);
            }
        }
    }

    private static Component discoverComponent(ServerLevel level, BlockPos origin) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Set<BlockPos> anchors = new HashSet<>();
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        addCandidate(level, origin, origin, blocks, open, anchors);
        for (Direction direction : Direction.values()) {
            addCandidate(level, origin, origin.relative(direction), blocks, open, anchors);
        }

        while (!open.isEmpty()) {
            BlockPos pos = open.removeFirst();
            for (Direction direction : Direction.values()) {
                addCandidate(level, origin, pos.relative(direction), blocks, open, anchors);
                if (blocks.size() > MAX_COMPONENT_BLOCKS) {
                    return new Component(Map.of(), Set.of(), true);
                }
            }
        }
        return new Component(blocks, anchors, false);
    }

    private static void addCandidate(
            ServerLevel level,
            BlockPos origin,
            BlockPos candidate,
            Map<BlockPos, BlockState> blocks,
            ArrayDeque<BlockPos> open,
            Set<BlockPos> anchors) {
        if (!withinBounds(candidate, origin) || !level.hasChunkAt(candidate)) {
            return;
        }

        BlockState state = level.getBlockState(candidate);
        if (!isStructuralBlock(state, level, candidate) || level.getBlockEntity(candidate) != null) {
            return;
        }
        if (isGroundAnchor(level, candidate)) {
            anchors.add(candidate.immutable());
            return;
        }
        if (blocks.putIfAbsent(candidate.immutable(), state) == null) {
            open.addLast(candidate.immutable());
        }
    }

    private static boolean withinBounds(BlockPos pos, BlockPos origin) {
        return Math.abs(pos.getX() - origin.getX()) <= MAX_HORIZONTAL_COMPONENT_RADIUS
                && Math.abs(pos.getY() - origin.getY()) <= MAX_VERTICAL_COMPONENT_RADIUS
                && Math.abs(pos.getZ() - origin.getZ()) <= MAX_HORIZONTAL_COMPONENT_RADIUS;
    }

    private static boolean isStructuralBlock(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.isAir() || state.getBlock() instanceof FallingBlock) {
            return false;
        }
        if (state.is(primitiveisolation.TIMBER_BEAM.get())) {
            return true;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (Block.isShapeFullBlock(shape)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (state.isFaceSturdy(level, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGroundAnchor(ServerLevel level, BlockPos pos) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() < surfaceY - 1) {
            return true;
        }
        if (pos.getY() != surfaceY - 1) {
            return false;
        }

        BlockPos belowPos = pos.below();
        return level.hasChunkAt(belowPos)
                && level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static void queueUnsupportedBlocks(ServerLevel level, Component component) {
        Map<BlockPos, Double> distances = calculateSupportDistances(level, component);
        LinkedHashSet<BlockPos> pending = PENDING_FALLS.computeIfAbsent(level, ignored -> new LinkedHashSet<>());
        component.blocks().forEach((pos, state) -> {
            double distance = distances.getOrDefault(pos, Double.POSITIVE_INFINITY);
            if (distance > strengthFor(state)) {
                pending.add(pos);
            } else {
                pending.remove(pos);
            }
        });
        if (pending.isEmpty()) {
            PENDING_FALLS.remove(level);
        }
    }

    private static Map<BlockPos, Double> calculateSupportDistances(ServerLevel level, Component component) {
        Map<BlockPos, Double> distances = new HashMap<>();
        PriorityQueue<PathNode> open = new PriorityQueue<>(Comparator.comparingDouble(PathNode::distance));

        for (Map.Entry<BlockPos, BlockState> entry : component.blocks().entrySet()) {
            if (touchesAnchor(level, entry.getKey(), component.anchors())) {
                distances.put(entry.getKey(), 0.0);
                open.add(new PathNode(entry.getKey(), 0.0));
            }
        }

        while (!open.isEmpty()) {
            PathNode current = open.remove();
            if (current.distance() != distances.getOrDefault(current.pos(), Double.POSITIVE_INFINITY)
                    || current.distance() > MAX_SEARCH_DISTANCE) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.pos().relative(direction);
                BlockState nextState = component.blocks().get(next);
                if (nextState == null) {
                    continue;
                }

                double candidateDistance = current.distance() + supportCost(direction, nextState);
                if (candidateDistance < distances.getOrDefault(next, Double.POSITIVE_INFINITY)) {
                    distances.put(next, candidateDistance);
                    open.add(new PathNode(next, candidateDistance));
                }
            }
        }
        return distances;
    }

    private static boolean touchesAnchor(ServerLevel level, BlockPos pos, Set<BlockPos> anchors) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (anchors.contains(neighbor) && level.hasChunkAt(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static double supportCost(Direction direction, BlockState state) {
        Block block = state.getBlock();
        if (direction.getAxis().isVertical()) {
            return block == primitiveisolation.SUPPORT_POST.get()
                    ? POST_SUPPORT_COST
                    : VERTICAL_SUPPORT_COST;
        }
        return block == primitiveisolation.TIMBER_BEAM.get() ? BEAM_SUPPORT_COST : 1.0;
    }

    private static double strengthFor(BlockState state) {
        Block block = state.getBlock();
        if (block == primitiveisolation.TIMBER_BEAM.get()) {
            return BEAM_STRENGTH;
        }
        if (block == primitiveisolation.SUPPORT_POST.get()
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)) {
            return WOOD_STRENGTH;
        }
        return DEFAULT_STRENGTH;
    }

    private static void processPendingFalls(ServerLevel level) {
        if (!Config.STRUCTURAL_INTEGRITY_ENABLED.getAsBoolean()) {
            PENDING_FALLS.remove(level);
            return;
        }

        LinkedHashSet<BlockPos> pending = PENDING_FALLS.get(level);
        if (pending == null) {
            return;
        }

        List<BlockPos> batch = new ArrayList<>(Math.min(MAX_FALLS_PER_TICK, pending.size()));
        var iterator = pending.iterator();
        while (iterator.hasNext() && batch.size() < MAX_FALLS_PER_TICK) {
            batch.add(iterator.next());
            iterator.remove();
        }

        for (BlockPos pos : batch) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (isStructuralBlock(state, level, pos)
                    && level.getBlockEntity(pos) == null
                    && !isGroundAnchor(level, pos)) {
                FallingBlockEntity.fall(level, pos, state);
            }
        }

        if (pending.isEmpty()) {
            PENDING_FALLS.remove(level);
        }
    }

    private record Component(Map<BlockPos, BlockState> blocks, Set<BlockPos> anchors, boolean overflowed) {}

    private record PathNode(BlockPos pos, double distance) {}
}
