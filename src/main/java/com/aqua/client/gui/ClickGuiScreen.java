package com.aqua.client.gui;

import com.aqua.client.AquaClient;
import com.aqua.client.config.HighlightSettings;
import com.aqua.client.render.BlockHighlighter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Compact four-column Aqua ClickGUI. */
public final class ClickGuiScreen extends Screen {
    private static final int CYAN = 0xFF00C9AE;
    private static final int PANEL = 0xE90A1113;
    private static final int COL_W = 150;
    private static final int GAP = 8;
    private static final String[] MODES = {"Outline", "Outline + Fill", "Filled"};
    private HighlightSettings selected;
    private String selectedName = "StorageFinder";

    public ClickGuiScreen() {
        super(Component.literal("Aqua Client"));
        selected = AquaClient.CONFIG.storage;
    }

    @Override
    protected void init() {
        clearWidgets();
        int totalWidth = COL_W * 4 + GAP * 3;
        int left = (width - totalWidth) / 2;
        int top = Math.max(24, (height - 250) / 2);
        int y = top + 30;

        addHeader(left, top, "RENDER");
        addHeader(left + COL_W + GAP, top, "VISUALS");
        addHeader(left + (COL_W + GAP) * 2, top, "EFFECTS");
        addHeader(left + (COL_W + GAP) * 3, top, "CLIENT");

        addButton(left, y, "StorageFinder" + (selectedName.equals("StorageFinder") ? "  <" : ""),
                () -> select("StorageFinder"));
        addButton(left, y + 25, "SpawnerFinder" + (selectedName.equals("SpawnerFinder") ? "  <" : ""),
                () -> select("SpawnerFinder"));

        int vx = left + COL_W + GAP;
        addButton(vx, y, "Enabled: " + onOff(selected.enabled), () -> change(() -> selected.enabled = !selected.enabled));
        addButton(vx, y + 25, "Mode: " + selected.mode, () -> change(() -> selected.mode = MODES[(indexOf(MODES, selected.mode) + 1) % MODES.length]));
        addButton(vx, y + 50, "Through walls: " + onOff(selected.throughWalls), () -> change(() -> selected.throughWalls = !selected.throughWalls));
        addButton(vx, y + 75, "Tracers: " + onOff(selected.tracers), () -> change(() -> selected.tracers = !selected.tracers));
        addButton(vx, y + 100, "Rainbow: " + onOff(selected.rainbow), () -> change(() -> selected.rainbow = !selected.rainbow));
        addButton(vx, y + 125, "Pulse: " + onOff(selected.pulse), () -> change(() -> selected.pulse = !selected.pulse));

        int ex = left + (COL_W + GAP) * 2;
        addButton(ex, y, "Colour: " + hex(selected.color), () -> change(() -> selected.color = nextColor(selected.color)));
        addButton(ex, y + 25, "Outline opacity: " + pct(selected.outlineOpacity), () -> change(() -> selected.outlineOpacity = step(selected.outlineOpacity)));
        addButton(ex, y + 50, "Fill opacity: " + pct(selected.fillOpacity), () -> change(() -> selected.fillOpacity = step(selected.fillOpacity)));
        addButton(ex, y + 75, "Thickness: " + selected.thickness, () -> change(() -> selected.thickness = selected.thickness >= 5 ? 1 : selected.thickness + 1));
        addButton(ex, y + 100, "Range: " + selected.range, () -> change(() -> selected.range = selected.range >= 64 ? 16 : selected.range + 8));

        int cx = left + (COL_W + GAP) * 3;
        addButton(cx, y, "Save settings", AquaClient::saveConfig);
        addButton(cx, y + 25, "Close", this::onClose);
    }

    private void addHeader(int x, int y, String title) {
        // Header labels are painted in render(); buttons are deliberately not used here.
    }

    private void addButton(int x, int y, String text, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> action.run())
                .bounds(x, y, COL_W, 20).build());
    }

    private void select(String name) {
        selectedName = name;
        selected = name.equals("StorageFinder") ? AquaClient.CONFIG.storage : AquaClient.CONFIG.spawner;
        init();
    }

    private void change(Runnable action) {
        action.run();
        init();
    }

    private static int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return i;
        return 0;
    }
    private static float step(float value) { return value >= 1.0f ? 0.0f : Math.min(1.0f, value + 0.1f); }
    private static String pct(float value) { return Math.round(value * 100) + "%"; }
    private static String onOff(boolean value) { return value ? "ON" : "OFF"; }
    private static String hex(int value) { return String.format("#%06X", value & 0xFFFFFF); }
    private static int nextColor(int color) {
        int[] colors = {0x35D6C2, 0x4CA3FF, 0xB56CFF, 0xFFB84D, 0xFF5D78, 0xFFFFFF};
        for (int i = 0; i < colors.length; i++) if (colors[i] == color) return colors[(i + 1) % colors.length];
        return colors[0];
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw a dim overlay directly; avoid the duplicate-blur crash on 1.21.11.
        graphics.fill(0, 0, width, height, 0x99000000);
        int totalWidth = COL_W * 4 + GAP * 3;
        int left = (width - totalWidth) / 2;
        int top = Math.max(24, (height - 250) / 2);
        String[] headings = {"RENDER", "VISUALS", "EFFECTS", "CLIENT"};
        for (int i = 0; i < 4; i++) {
            int x = left + i * (COL_W + GAP);
            graphics.fill(x, top, x + COL_W, top + 235, PANEL);
            graphics.fill(x, top, x + COL_W, top + 22, CYAN);
            graphics.drawCenteredString(font, headings[i], x + COL_W / 2, top + 7, 0xFF001B19);
        }
        graphics.drawCenteredString(font, "A Q U A   C L I E N T", width / 2, top - 16, 0xFFE8FFFF);
        graphics.drawString(font, "Selected: " + selectedName, left + 5, top + 212, 0xFF9BB8B5, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() { AquaClient.saveConfig(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
