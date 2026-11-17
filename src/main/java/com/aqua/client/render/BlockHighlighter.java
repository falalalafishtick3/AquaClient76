package com.aqua.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockHighlighter {

    private static boolean enabled = true;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockHighlighter::render);
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        // Logica ta de tick aici
    }

    private static void render(WorldRenderContext context) {
        if (!enabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        HitResult hit = client.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos target = ((BlockHitResult) hit).getBlockPos();
        Vec3 camera = context.camera().getPosition();

        AABB box = new AABB(target).inflate(0.002).move(-camera.x, -camera.y, -camera.z);

        // Aici desenezi conturul folosind box si context.matrixStack() / context.consumers()
    }
}
