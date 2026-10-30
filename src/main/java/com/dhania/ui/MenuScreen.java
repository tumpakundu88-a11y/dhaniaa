package com.dhania.ui;

import com.dhania.config.Config;
import com.dhania.module.ClientModule;
import com.dhania.module.ClientModule.Category;
import com.dhania.module.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MenuScreen extends Screen {
    private static final int PW = 480, PH = 260, SB = 120;
    private static Category tab = Category.HUD;

    public MenuScreen() { super(Text.literal("Dhania Client")); }

    @Override public boolean shouldPause() { return false; }

    // we draw our own background in render()
    @Override public void renderBackground(DrawContext c, int mx, int my, float d) {}

    private List<ClientModule> mods() {
        List<ClientModule> r = new ArrayList<>();
        for (ClientModule m : ModuleManager.ALL) if (m.category == tab) r.add(m);
        return r;
    }

    private int cardX(int i, int px) { return px + SB + 10 + (i % 2) * 175; }
    private int cardY(int i, int py) { return py + 44 + (i / 2) * 42; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x90000000);
        int px = (width - PW) / 2, py = (height - PH) / 2;

        c.fill(px, py, px + PW, py + PH, Theme.BG);
        c.fill(px, py, px + SB, py + PH, Theme.PANEL);
        c.fill(px + SB, py, px + SB + 1, py + PH, Theme.ACCENT_DARK);
        c.drawBorder(px, py, PW, PH, Theme.ACCENT_DARK);

        c.drawText(textRenderer, Text.literal("DHANIA").formatted(Formatting.BOLD), px + 14, py + 12, Theme.ACCENT, true);
        c.drawText(textRenderer, "C L I E N T", px + 14, py + 26, Theme.MUTED, false);

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int by = py + 52 + i * 24;
            Theme.button(c, textRenderer, px + 10, by, SB - 20, 20, cats[i].label,
                    Theme.inside(mx, my, px + 10, by, SB - 20, 20), cats[i] == tab);
        }
        Theme.button(c, textRenderer, px + 10, py + PH - 54, SB - 20, 20, "Waypoints",
                Theme.inside(mx, my, px + 10, py + PH - 54, SB - 20, 20), false);
        Theme.button(c, textRenderer, px + 10, py + PH - 28, SB - 20, 20, "Edit HUD",
                Theme.inside(mx, my, px + 10, py + PH - 28, SB - 20, 20), false);

        c.drawText(textRenderer, tab.label + " Modules", px + SB + 10, py + 14, Theme.ACCENT, false);
        c.drawText(textRenderer, "Click a module to enable / disable", px + SB + 10, py + 27, Theme.MUTED, false);

        List<ClientModule> list = mods();
        for (int i = 0; i < list.size(); i++) {
            ClientModule m = list.get(i);
            int x = cardX(i, px), y = cardY(i, py);
            boolean hov = Theme.inside(mx, my, x, y, 165, 36);
            c.fill(x, y, x + 165, y + 36, hov ? Theme.CARD_HOVER : Theme.CARD);
            c.fill(x, y, x + 2, y + 36, m.enabled ? Theme.ACCENT : 0xFF2A3A31);
            c.drawText(textRenderer, m.name, x + 9, y + 7, Theme.TEXT, false);
            c.drawText(textRenderer, textRenderer.trimToWidth(m.description, 112), x + 9, y + 21, Theme.MUTED, false);
            Theme.toggle(c, x + 165 - 32, y + 12, m.enabled);
        }

        c.drawText(textRenderer, "RShift: menu   B: waypoints   C: zoom", px + SB + 10, py + PH - 14, Theme.MUTED, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int px = (width - PW) / 2, py = (height - PH) / 2;
        if (click.button() == 0) {
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++)
                if (Theme.inside(mx, my, px + 10, py + 52 + i * 24, SB - 20, 20)) { tab = cats[i]; return true; }
            if (Theme.inside(mx, my, px + 10, py + PH - 54, SB - 20, 20)) { client.setScreen(new WaypointScreen()); return true; }
            if (Theme.inside(mx, my, px + 10, py + PH - 28, SB - 20, 20)) { client.setScreen(new HudEditorScreen()); return true; }
            List<ClientModule> list = mods();
            for (int i = 0; i < list.size(); i++)
                if (Theme.inside(mx, my, cardX(i, px), cardY(i, py), 165, 36)) {
                    list.get(i).toggle();
                    Config.save();
                    return true;
                }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.getKeycode() == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(input);
    }
}
