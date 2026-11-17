package com.aqua.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockHighlighter {

    private static boolean enabled = true;
    private static BlockPos currentTarget = null;

    // Culoare contur (RGBA: Cyan / Aqua)
    private static float red = 0.0f;
    private static float green = 0.85f;
    private static float blue = 1.0f;
    private static float alpha = 0.9f;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockHighlighter::render);
    }

    public static void tick(Minecraft client) {
        if (!enabled || client.player == null || client.level == null) {
            currentTarget = null;
            return;
        }

        HitResult hit = client.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            currentTarget = ((BlockHitResult) hit).getBlockPos();
        } else {
            currentTarget = null;
        }
    }

    private static void render(WorldRenderContext context) {
        if (!enabled || currentTarget == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) {
            return;
        }

        Vec3 camera = client.gameRenderer.getMainCamera().getPosition();
        AABB box = new AABB(currentTarget)
                .inflate(0.002)
                .move(-camera.x, -camera.y, -camera.z);

        VertexConsumer buffer = consumers.getBuffer(RenderType.lines());
        ShapeRenderer.renderLineBox(matrices, buffer, box, red, green, blue, alpha);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean state) {
        enabled = state;
        if (!state) {
            currentTarget = null;
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static void setColor(float r, float g, float b, float a) {
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }
}
