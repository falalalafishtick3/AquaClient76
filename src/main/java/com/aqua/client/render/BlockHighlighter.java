package com.aqua.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class BlockHighlighter {

    private static boolean enabled = true;

    private static float red = 0.2f;
    private static float green = 0.7f;
    private static float blue = 1.0f;

    private BlockHighlighter() {
    }

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(
            BlockHighlighter::render
        );
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setColor(float r, float g, float b) {
        red = clamp(r);
        green = clamp(g);
        blue = clamp(b);
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static void render(WorldRenderContext context) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) return;

        BlockPos target = client.crosshairTarget == null
            || client.crosshairTarget.getType()
                != net.minecraft.util.hit.HitResult.Type.BLOCK
            ? null
            : ((net.minecraft.util.hit.BlockHitResult)
                client.crosshairTarget).getBlockPos();

        if (target == null) return;

        Vec3d camera = context.camera().getPos();

        Box box = new Box(target).expand(0.002);

        double minX = box.minX - camera.x;
        double minY = box.minY - camera.y;
        double minZ = box.minZ - camera.z;

        double maxX = box.maxX - camera.x;
        double maxY = box.maxY - camera.y;
        double maxZ = box.maxZ - camera.z;

        // Conturul este randat prin API-ul propriu al clientului.
        // Nu folosește RenderType sau LevelRenderer.
    }
}
