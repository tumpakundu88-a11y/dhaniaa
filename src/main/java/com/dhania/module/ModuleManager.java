package com.dhania.module;

import com.dhania.module.ClientModule.Category;
import com.dhania.ui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    public static final List<ClientModule> ALL = new ArrayList<>();
    public static KeyBinding ZOOM_KEY;

    public static ClientModule FULLBRIGHT, WAYPOINTS;

    // ---- combat stats ----
    private static int combo;
    private static double reach;
    private static long lastHit;
    private static int prevHurt;
    private static LivingEntity target;
    private static long targetTime;

    // ---- CPS ----
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();
    private static boolean lastL, lastR;

    private ModuleManager() {}

    private static void add(ClientModule m) { ALL.add(m); }

    public static boolean isEnabled(ClientModule m) { return m != null && m.enabled; }

    // =============== per-frame / per-tick helpers ===============
    public static void pollClicks(MinecraftClient mc) {
        long h = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (mc.currentScreen == null) {
            if (l && !lastL) LEFT.add(now);
            if (r && !lastR) RIGHT.add(now);
        }
        lastL = l;
        lastR = r;
    }

    private static int cps(ArrayDeque<Long> q) {
        long now = System.currentTimeMillis();
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        return q.size();
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (mc.player.hurtTime > 0 && prevHurt == 0) combo = 0;
        prevHurt = mc.player.hurtTime;
        for (ClientModule m : ALL) if (m.enabled) m.onTick(mc);
    }

    public static void onAttack(PlayerEntity p, Entity e, EntityHitResult hit) {
        if (!(e instanceof LivingEntity)) return;
        combo++;
        lastHit = System.currentTimeMillis();
        if (hit != null) reach = p.getEyePos().distanceTo(hit.getPos());
    }

    // =============== helpers ===============
    private static String dir(float yaw) {
        String[] d = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return d[Math.floorMod(Math.round(yaw / 45f), 8)];
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    private static String time(int ticks) {
        int s = ticks / 20;
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }

    private static void keyBox(DrawContext c, TextRenderer tr, int x, int y, int w, int h, String s, boolean down) {
        c.fill(x, y, x + w, y + h, down ? Theme.ACCENT : Theme.HUD_BG);
        c.drawBorder(x, y, w, h, down ? Theme.ACCENT : Theme.ACCENT_DARK);
        c.drawText(tr, s, x + (w - tr.getWidth(s)) / 2, y + (h - 8) / 2, down ? 0xFF06140C : Theme.TEXT, false);
    }

    // =============== module registry ===============
    public static void init() {
        ALL.clear();

        // ---------------- HUD ----------------
        add(new ClientModule("FPS", "Shows your frames per second", Category.HUD, true, 6, 6, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                Theme.textBox(c, mc.textRenderer, this, "FPS", String.valueOf(mc.getCurrentFps()));
            }
        });

        add(new ClientModule("CPS", "Left | Right clicks per second", Category.HUD, true, 6, 22, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                Theme.textBox(c, mc.textRenderer, this, "CPS", cps(LEFT) + " | " + cps(RIGHT));
            }
        });

        add(new ClientModule("Ping", "Server latency in ms", Category.HUD, true, 6, 38, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                int ping = 0;
                if (mc.getNetworkHandler() != null && mc.player != null) {
                    PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                    if (e != null) ping = e.getLatency();
                }
                Theme.textBox(c, mc.textRenderer, this, "PING", ping + " ms");
            }
        });

        add(new ClientModule("Coordinates", "XYZ and facing direction", Category.HUD, true, 6, 54, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                if (mc.player == null) return;
                String v = mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ()
                        + "  " + dir(mc.player.getYaw());
                Theme.textBox(c, mc.textRenderer, this, "XYZ", v);
            }
        });

        add(new ClientModule("Keystrokes", "WASD + mouse buttons overlay", Category.HUD, true, 6, 176, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                TextRenderer tr = mc.textRenderer;
                w = 60; h = 58;
                var o = mc.options;
                keyBox(c, tr, x + 20, y, 18, 18, "W", o.forwardKey.isPressed());
                keyBox(c, tr, x, y + 20, 18, 18, "A", o.leftKey.isPressed());
                keyBox(c, tr, x + 20, y + 20, 18, 18, "S", o.backKey.isPressed());
                keyBox(c, tr, x + 40, y + 20, 18, 18, "D", o.rightKey.isPressed());
                keyBox(c, tr, x, y + 40, 29, 18, "LMB", o.attackKey.isPressed());
                keyBox(c, tr, x + 31, y + 40, 29, 18, "RMB", o.useKey.isPressed());
            }
        });

        add(new ClientModule("Potion Status", "Active effects with timers", Category.HUD, true, 100, 6, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                if (mc.player == null) return;
                List<String> lines = new ArrayList<>();
                for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                    if (isEnabled(FULLBRIGHT) && e.getEffectType().equals(StatusEffects.NIGHT_VISION)) continue;
                    lines.add(e.getEffectType().value().getName().getString() + " " + roman(e.getAmplifier() + 1)
                            + "  " + (e.isInfinite() ? "**" : time(e.getDuration())));
                }
                if (lines.isEmpty()) return;
                TextRenderer tr = mc.textRenderer;
                int mw = 0;
                for (String s : lines) mw = Math.max(mw, tr.getWidth(s));
                w = mw + 12;
                h = lines.size() * 12 + 4;
                c.fill(x, y, x + w, y + h, Theme.HUD_BG);
                c.fill(x, y, x + 2, y + h, Theme.ACCENT);
                for (int i = 0; i < lines.size(); i++)
                    c.drawText(tr, lines.get(i), x + 7, y + 4 + i * 12, Theme.TEXT, false);
            }
        });

        // ---------------- PvP ----------------
        add(new ClientModule("Armor HUD", "Armor + held item with durability", Category.PVP, true, 6, 74, true) {
            private final EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
            @Override public void render(DrawContext c, MinecraftClient mc) {
                if (mc.player == null) return;
                TextRenderer tr = mc.textRenderer;
                w = 46; h = 90;
                for (int i = 0; i < slots.length; i++) {
                    ItemStack s = mc.player.getEquippedStack(slots[i]);
                    if (s.isEmpty()) continue;
                    int ry = y + i * 18;
                    c.fill(x, ry, x + w, ry + 17, Theme.HUD_BG);
                    c.fill(x, ry, x + 2, ry + 17, Theme.ACCENT);
                    c.drawItem(s, x + 3, ry);
                    c.drawStackOverlay(tr, s, x + 3, ry);
                    if (s.isDamageable()) {
                        int pct = (s.getMaxDamage() - s.getDamage()) * 100 / s.getMaxDamage();
                        int col = pct > 60 ? 0xFF2ECC71 : pct > 30 ? 0xFFF1C40F : 0xFFE74C3C;
                        c.drawText(tr, pct + "%", x + 21, ry + 5, col, false);
                    }
                }
            }
        });

        add(new ClientModule("Combo Counter", "Consecutive hits without being hit", Category.PVP, true, 100, 22, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                int v = System.currentTimeMillis() - lastHit > 3000 ? 0 : combo;
                Theme.textBox(c, mc.textRenderer, this, "COMBO", String.valueOf(v));
            }
        });

        add(new ClientModule("Reach Display", "Distance of your last hit", Category.PVP, true, 100, 38, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                Theme.textBox(c, mc.textRenderer, this, "REACH", String.format("%.2f", reach));
            }
        });

        add(new ClientModule("Target HUD", "Name + health of who you hit", Category.PVP, true, 100, 54, true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                long now = System.currentTimeMillis();
                if (mc.targetedEntity instanceof LivingEntity t) { target = t; targetTime = now; }
                if (target == null || now - targetTime > 2500 || !target.isAlive()) return;
                TextRenderer tr = mc.textRenderer;
                w = 120; h = 30;
                c.fill(x, y, x + w, y + h, Theme.HUD_BG);
                c.fill(x, y, x + 2, y + h, Theme.ACCENT);
                c.drawText(tr, tr.trimToWidth(target.getName().getString(), 112), x + 6, y + 4, Theme.TEXT, false);
                float hp = Math.max(0, target.getHealth()), max = Math.max(1, target.getMaxHealth());
                c.fill(x + 6, y + 18, x + 114, y + 24, 0xFF1A2A21);
                c.fill(x + 6, y + 18, x + 6 + (int) (108 * Math.min(1f, hp / max)), y + 24, Theme.ACCENT);
                String t = String.format("%.1f", hp);
                c.drawText(tr, t, x + 114 - tr.getWidth(t), y + 4 + 0, Theme.ACCENT, false);
            }
        });

        add(new ClientModule("Zoom", "Hold C to zoom in", Category.PVP, false, 0, 0, true) {
            private boolean zooming;
            private int saved = 70;
            @Override public void onTick(MinecraftClient mc) {
                SimpleOption<Integer> fov = mc.options.getFov();
                boolean want = ZOOM_KEY != null && ZOOM_KEY.isPressed() && mc.currentScreen == null;
                if (want && !zooming) { saved = fov.getValue(); zooming = true; }
                if (want) { if (fov.getValue() != 30) fov.setValue(30); }
                else if (zooming) { fov.setValue(saved); zooming = false; }
            }
            @Override public void onDisable() {
                if (zooming) { MinecraftClient.getInstance().options.getFov().setValue(saved); zooming = false; }
            }
        });

        FULLBRIGHT = new ClientModule("Fullbright", "See clearly in caves & at night", Category.PVP, false, 0, 0, false) {
            @Override public void onTick(MinecraftClient mc) {
                StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                if (cur == null || cur.getDuration() < 600)
                    mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0, false, false, false));
            }
            @Override public void onDisable() {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        };
        add(FULLBRIGHT);

        add(new ClientModule("Toggle Sprint", "Auto-sprint while moving forward", Category.PVP, false, 0, 0, false) {
            @Override public void onTick(MinecraftClient mc) {
                if (mc.currentScreen == null && mc.options.forwardKey.isPressed() && !mc.player.isSneaking()
                        && !mc.player.horizontalCollision && mc.player.getHungerManager().getFoodLevel() > 6)
                    mc.player.setSprinting(true);
            }
        });

        // ---------------- Utility ----------------
        WAYPOINTS = new ClientModule("Waypoints", "Show waypoint markers in-world", Category.UTILITY, false, 0, 0, true) {};
        add(WAYPOINTS);
    }
}
