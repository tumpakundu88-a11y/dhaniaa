package com.dhania.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class ClientModule {
    public enum Category {
        HUD("HUD"), PVP("PvP"), UTILITY("Utility");
        public final String label;
        Category(String l) { label = l; }
    }

    public final String name, description;
    public final Category category;
    public final boolean hud;
    public boolean enabled;
    public int x, y, w = 80, h = 14;

    public ClientModule(String name, String description, Category category,
                        boolean hud, int x, int y, boolean enabledByDefault) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.hud = hud;
        this.x = x;
        this.y = y;
        this.enabled = enabledByDefault;
    }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (enabled == e) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void render(DrawContext c, MinecraftClient mc) {}
}
