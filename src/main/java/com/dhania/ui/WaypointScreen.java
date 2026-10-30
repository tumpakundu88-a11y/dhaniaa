package com.dhania.ui;

import com.dhania.config.Config;
import com.dhania.waypoint.Waypoint;
import com.dhania.waypoint.WaypointManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaypointScreen extends Screen {
    private static final int PW = 340, PH = 236, ROW = 22, VIS = 5;
    private TextFieldWidget search, name, fx, fy, fz;
    private int scroll;
    private String status = "";

    public WaypointScreen() { super(Text.literal("Waypoints")); }

    @Override public boolean shouldPause() { return false; }

    @Override public void renderBackground(DrawContext c, int mx, int my, float d) {}

    @Override
    protected void init() {
        int px = (width - PW) / 2, py = (height - PH) / 2;
        search = field(px + 10, py + 30, PW - 20, "Search waypoints...");
        search.setChangedListener(s -> scroll = 0);
        name = field(px + 10, py + PH - 58, 110, "Name");
        name.setMaxLength(24);
        fx = field(px + 126, py + PH - 58, 62, "X");
        fy = field(px + 192, py + PH - 58, 62, "Y (optional)");
        fz = field(px + 258, py + PH - 58, 62, "Z");
    }

    private TextFieldWidget field(int x, int y, int w, String hint) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 16, Text.literal(hint));
        f.setPlaceholder(Text.literal(hint));
        addDrawableChild(f);
        return f;
    }

    private static String coords(Waypoint w) {
        return (int) w.x + ", " + (int) w.y + ", " + (int) w.z;
    }

    private List<Waypoint> filtered() {
        String q = search.getText().toLowerCase(Locale.ROOT).trim();
        List<Waypoint> r = new ArrayList<>();
        for (Waypoint w : WaypointManager.LIST)
            if (q.isEmpty() || w.name.toLowerCase(Locale.ROOT).contains(q) || coords(w).contains(q)) r.add(w);
        return r;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x90000000);
        int px = (width - PW) / 2, py = (height - PH) / 2;
        c.fill(px, py, px + PW, py + PH, Theme.BG);
        c.drawBorder(px, py, PW, PH, Theme.ACCENT_DARK);
        c.fill(px, py, px + PW, py + 22, Theme.PANEL);
        c.drawText(textRenderer, "WAYPOINTS", px + 10, py + 7, Theme.ACCENT, true);

        List<Waypoint> list = filtered();
        int listY = py + 52;
        if (list.isEmpty())
            c.drawText(textRenderer, "No waypoints yet - add one below.", px + 10, listY + 8, Theme.MUTED, false);
        for (int i = 0; i < VIS && i + scroll < list.size(); i++) {
            Waypoint w = list.get(i + scroll);
            int ry = listY + i * ROW;
            c.fill(px + 10, ry, px + PW - 10, ry + ROW - 2, Theme.CARD);
            c.fill(px + 10, ry, px + 13, ry + ROW - 2, w.color);
            c.drawText(textRenderer, textRenderer.trimToWidth(w.name, 190), px + 18, ry + 2, Theme.TEXT, false);
            c.drawText(textRenderer, coords(w) + "  -  " + (int) WaypointManager.distance(client, w) + "m",
                    px + 18, ry + 12, Theme.MUTED, false);
            Theme.button(c, textRenderer, px + PW - 72, ry + 2, 40, 16, w.visible ? "Hide" : "Show",
                    Theme.inside(mx, my, px + PW - 72, ry + 2, 40, 16), false);
            Theme.button(c, textRenderer, px + PW - 30, ry + 2, 16, 16, "x",
                    Theme.inside(mx, my, px + PW - 30, ry + 2, 16, 16), false);
        }

        c.drawText(textRenderer, "Add new waypoint", px + 10, py + PH - 70, Theme.ACCENT, false);
        Theme.button(c, textRenderer, px + 10, py + PH - 36, 150, 16, "Use my position",
                Theme.inside(mx, my, px + 10, py + PH - 36, 150, 16), false);
        Theme.button(c, textRenderer, px + 170, py + PH - 36, 160, 16, "Add Waypoint",
                Theme.inside(mx, my, px + 170, py + PH - 36, 160, 16), true);
        c.drawText(textRenderer, status, px + 10, py + PH - 14, Theme.MUTED, false);

        super.render(c, mx, my, delta); // draws text fields
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() != 0) return false;
        double mx = click.x(), my = click.y();
        int px = (width - PW) / 2, py = (height - PH) / 2;
        List<Waypoint> list = filtered();
        for (int i = 0; i < VIS && i + scroll < list.size(); i++) {
            Waypoint w = list.get(i + scroll);
            int ry = py + 52 + i * ROW;
            if (Theme.inside(mx, my, px + PW - 72, ry + 2, 40, 16)) { w.visible = !w.visible; Config.save(); return true; }
            if (Theme.inside(mx, my, px + PW - 30, ry + 2, 16, 16)) {
                WaypointManager.LIST.remove(w);
                Config.save();
                status = "Deleted " + w.name;
                return true;
            }
        }
        if (Theme.inside(mx, my, px + 10, py + PH - 36, 150, 16) && client.player != null) {
            fx.setText(String.valueOf(client.player.getBlockX()));
            fy.setText(String.valueOf(client.player.getBlockY()));
            fz.setText(String.valueOf(client.player.getBlockZ()));
            return true;
        }
        if (Theme.inside(mx, my, px + 170, py + PH - 36, 160, 16)) { addFromFields(); return true; }
        return false;
    }

    private void addFromFields() {
        try {
            double x = Double.parseDouble(fx.getText().trim());
            double z = Double.parseDouble(fz.getText().trim());
            String yt = fy.getText().trim();
            double y = yt.isEmpty() ? (client.player != null ? client.player.getY() : 64) : Double.parseDouble(yt);
            String n = name.getText().trim();
            if (n.isEmpty()) n = "Waypoint " + (WaypointManager.LIST.size() + 1);
            WaypointManager.add(n, x, y, z);
            status = "Added " + n;
            name.setText(""); fx.setText(""); fy.setText(""); fz.setText("");
        } catch (NumberFormatException e) {
            status = "X and Z must be numbers";
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        int max = Math.max(0, filtered().size() - VIS);
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(v)));
        return true;
    }
}
