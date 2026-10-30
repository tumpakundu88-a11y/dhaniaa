package com.dhania.ui;

import com.dhania.module.ClientModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Theme {
    public static final int BG = 0xF00A120D;
    public static final int PANEL = 0xFF101A14;
    public static final int CARD = 0xFF15231B;
    public static final int CARD_HOVER = 0xFF1B2F23;
    public static final int ACCENT = 0xFF2ECC71;
    public static final int ACCENT_DARK = 0xFF1E8449;
    public static final int TEXT = 0xFFE9FFF1;
    public static final int MUTED = 0xFF7FA38E;
    public static final int HUD_BG = 0x90000000;

    private Theme() {}

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Standard HUD pill: green accent bar, green label, white value. */
    public static void textBox(DrawContext c, TextRenderer tr, ClientModule m, String label, String value) {
        int lw = tr.getWidth(label), vw = tr.getWidth(value);
        m.w = lw + vw + 14;
        m.h = 14;
        c.fill(m.x, m.y, m.x + m.w, m.y + m.h, HUD_BG);
        c.fill(m.x, m.y, m.x + 2, m.y + m.h, ACCENT);
        c.drawText(tr, label, m.x + 6, m.y + 3, ACCENT, false);
        c.drawText(tr, value, m.x + 9 + lw, m.y + 3, TEXT, false);
    }

    public static void button(DrawContext c, TextRenderer tr, int x, int y, int w, int h,
                              String label, boolean hover, boolean active) {
        c.fill(x, y, x + w, y + h, active ? ACCENT_DARK : (hover ? CARD_HOVER : CARD));
        c.drawBorder(x, y, w, h, (hover || active) ? ACCENT : ACCENT_DARK);
        c.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, TEXT, false);
    }

    public static void toggle(DrawContext c, int x, int y, boolean on) {
        c.fill(x, y, x + 24, y + 12, on ? ACCENT_DARK : 0xFF2A3A31);
        c.drawBorder(x, y, 24, 12, on ? ACCENT : 0xFF3E5246);
        int kx = on ? x + 13 : x + 2;
        c.fill(kx, y + 2, kx + 9, y + 10, on ? ACCENT : MUTED);
    }
}
