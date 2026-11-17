package com.aqua.client;

import com.aqua.client.config.ClientConfig;
import com.aqua.client.gui.ClickGuiScreen;
import com.aqua.client.render.BlockHighlighter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class AquaClient implements ClientModInitializer {
    public static final String MOD_ID = "aquaclient";
    public static final ClientConfig CONFIG = new ClientConfig();
    private static KeyMapping clickGuiKey;

    @Override
    public void onInitializeClient() {
        CONFIG.load();
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.aquaclient.clickgui", GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC));
        BlockHighlighter.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clickGuiKey.consumeClick()) {
                if (client.screen == null) client.setScreen(new ClickGuiScreen());
            }
            BlockHighlighter.tick(client);
        });
    }

    public static void saveConfig() { CONFIG.save(); }
}
