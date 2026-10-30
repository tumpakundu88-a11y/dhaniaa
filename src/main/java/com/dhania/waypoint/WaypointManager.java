package com.dhania.waypoint;

import com.dhania.config.Config;
import com.dhania.module.ModuleManager;
import com.dhania.ui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class WaypointManager {
    public static final List<Waypoint> LIST = new ArrayList<>();
    private static final int[] PALETTE = {0xFF2ECC71, 0xFF3498DB, 0xFFE74C3C, 0xFFF1C40F, 0xFF9B59B6, 0xFF1ABC9C};

    private WaypointManager() {}

    public static String currentDim(MinecraftClient mc) {
        return mc.world == null ? "minecraft:overworld" : mc.world.getRegistryKey().getValue().toString();
    }

    public static void add(String name, double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        LIST.add(new Waypoint(name, x, y, z, currentDim(mc), PALETTE[LIST.size() % PALETTE.length]));
        Config.save();
    }

    public static double distance(MinecraftClient mc, Waypoint w) {
        if (mc.player == null) return 0;
        double dx = w.x + 0.5 - mc.player.getX(), dy = w.y - mc.player.getY(), dz = w.z + 0.5 - mc.player.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Projects waypoints onto the screen using the camera orientation (no matrix/mixin needed). */
    public static void render(DrawContext c, MinecraftClient mc) {
        if (!ModuleManager.isEnabled(ModuleManager.WAYPOINTS) || LIST.isEmpty() || mc.world == null) return;
        Camera cam = mc.gameRenderer.getCamera();
        Vec3d cp = cam.getCameraPos();
        double yaw = Math.toRadians(cam.getYaw()), pitch = Math.toRadians(cam.getPitch());
        double sy = Math.sin(yaw), cy = Math.cos(yaw), sp = Math.sin(pitch), cpch = Math.cos(pitch);
        double fx = -sy * cpch, fy = -sp, fz = cy * cpch;       // forward
        double rx = -cy, rz = -sy;                               // right
        double ux = -sy * sp, uy = cpch, uz = cy * sp;           // up
        int W = mc.getWindow().getScaledWidth(), H = mc.getWindow().getScaledHeight();
        double scale = (H / 2.0) / Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        String dim = currentDim(mc);
        TextRenderer tr = mc.textRenderer;

        for (Waypoint w : LIST) {
            if (!w.visible || !dim.equals(w.dim)) continue;
            double dx = w.x + 0.5 - cp.x, dy = w.y + 1.0 - cp.y, dz = w.z + 0.5 - cp.z;
            double z = dx * fx + dy * fy + dz * fz;
            double xx = dx * rx + dz * rz;
            double yy = dx * ux + dy * uy + dz * uz;
            double px, py;
            if (z > 0.1) {
                px = W / 2.0 + xx / z * scale;
                py = H / 2.0 - yy / z * scale;
            } else {
                px = xx >= 0 ? W : 0;
                py = H / 2.0;
            }
            int sx = (int) Math.max(14, Math.min(W - 14, px));
            int sY = (int) Math.max(22, Math.min(H - 26, py));

            for (int i = -4; i <= 4; i++) {
                int hw = 4 - Math.abs(i);
                c.fill(sx - hw, sY + i, sx + hw + 1, sY + i + 1, w.color);
            }
            String d = (int) distance(mc, w) + "m";
            int nw = tr.getWidth(w.name);
            c.fill(sx - nw / 2 - 3, sY - 17, sx + nw / 2 + 4, sY - 6, Theme.HUD_BG);
            c.drawText(tr, w.name, sx - nw / 2, sY - 15, w.color, false);
            c.drawText(tr, d, sx - tr.getWidth(d) / 2, sY + 7, Theme.TEXT, true);
        }
    }
}
