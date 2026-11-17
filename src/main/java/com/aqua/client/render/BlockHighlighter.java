package com.aqua.client.render;

import com.aqua.client.AquaClient;
import com.aqua.client.config.HighlightSettings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Set;

/** Scans loaded client-world blocks and renders outlines around matching blocks. */
public final class BlockHighlighter {
    private static final Set<BlockPos> STORAGE = new LinkedHashSet<>();
    private static final Set<BlockPos> SPAWNERS = new LinkedHashSet<>();
    private static int scanDelay;

    private BlockHighlighter() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockHighlighter::render);
    }

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            STORAGE.clear();
            SPAWNERS.clear();
            scanDelay = 0;
            return;
        }
        // Rescan twice per second rather than doing a costly full scan every tick.
        if (++scanDelay < 10) return;
        scanDelay = 0;

        STORAGE.clear();
        SPAWNERS.clear();
        HighlightSettings storageSettings = AquaClient.CONFIG.storage;
        HighlightSettings spawnerSettings = AquaClient.CONFIG.spawner;
        if (!storageSettings.enabled && !spawnerSettings.enabled) return;

        int storageRange = storageSettings.enabled ? clampRange(storageSettings.range) : 0;
        int spawnerRange = spawnerSettings.enabled ? clampRange(spawnerSettings.range) : 0;
        int radius = Math.max(storageRange, spawnerRange);
        BlockPos origin = client.player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = Math.max(client.level.getMinY(), origin.getY() - 24);
        int maxY = Math.min(client.level.getMaxY() - 1, origin.getY() + 24);

        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                int horizontalDistanceSquared = dx * dx + dz * dz;
                if (horizontalDistanceSquared > radius * radius) continue;
                // Avoid forcing chunk loads; only inspect chunks already present client-side.
                pos.set(x, origin.getY(), z);
                if (!client.level.hasChunkAt(pos)) continue;

                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    Block block = client.level.getBlockState(pos).getBlock();
                    if (storageRange > 0 && horizontalDistanceSquared <= storageRange * storageRange
                            && isStorage(block)) {
                        STORAGE.add(pos.immutable());
                    }
                    if (spawnerRange > 0 && horizontalDistanceSquared <= spawnerRange * spawnerRange
                            && block == Blocks.SPAWNER) {
                        SPAWNERS.add(pos.immutable());
                    }
                }
            }
        }
    }

    private static int clampRange(int range) {
        return Math.max(8, Math.min(range, 64));
    }

    private static boolean isStorage(Block block) {
        return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST
                || block == Blocks.ENDER_CHEST || block == Blocks.DROPPER
                || block == Blocks.DISPENSER || block == Blocks.BARREL
                || block instanceof ShulkerBoxBlock;
    }

    private static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || context.matrices() == null
                || context.consumers() == null) return;
        renderSet(context, STORAGE, AquaClient.CONFIG.storage);
        renderSet(context, SPAWNERS, AquaClient.CONFIG.spawner);
    }

    private static void renderSet(WorldRenderContext context, Set<BlockPos> blocks,
                                  HighlightSettings settings) {
        if (!settings.enabled || blocks.isEmpty()) return;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        PoseStack matrices = context.matrices();
        VertexConsumer lines = context.consumers().getBuffer(RenderType.lines());
        int rgb = settings.rainbow ? rainbowColor() : settings.color;
        float red = ((rgb >> 16) & 255) / 255.0f;
        float green = ((rgb >> 8) & 255) / 255.0f;
        float blue = (rgb & 255) / 255.0f;
        float alpha = clamp01(settings.outlineOpacity);
        double maxDistanceSquared = (double) clampRange(settings.range) * clampRange(settings.range);

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        for (BlockPos pos : blocks) {
            if (camera.distanceToSqr(Vec3.atCenterOf(pos)) > maxDistanceSquared) continue;
            LevelRenderer.renderLineBox(matrices, lines, new AABB(pos), red, green, blue, alpha);
        }
        matrices.popPose();
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int rainbowColor() {
        float hue = (System.currentTimeMillis() % 6000L) / 6000.0f;
        return Color.HSBtoRGB(hue, 0.82f, 1.0f) & 0xFFFFFF;
    }

    public static int getStorageCount() { return STORAGE.size(); }
    public static int getSpawnerCount() { return SPAWNERS.size(); }
}
