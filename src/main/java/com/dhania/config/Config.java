package com.dhania.config;

import com.dhania.module.ClientModule;
import com.dhania.module.ModuleManager;
import com.dhania.waypoint.Waypoint;
import com.dhania.waypoint.WaypointManager;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("dhania-client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonObject mods = root.getAsJsonObject("modules");
            if (mods != null) {
                for (ClientModule m : ModuleManager.ALL) {
                    JsonObject o = mods.getAsJsonObject(m.name);
                    if (o == null) continue;
                    m.enabled = o.get("enabled").getAsBoolean();
                    m.x = o.get("x").getAsInt();
                    m.y = o.get("y").getAsInt();
                }
            }
            JsonElement wp = root.get("waypoints");
            if (wp != null) {
                List<Waypoint> list = GSON.fromJson(wp, new TypeToken<List<Waypoint>>() {}.getType());
                WaypointManager.LIST.clear();
                if (list != null) WaypointManager.LIST.addAll(list);
            }
        } catch (Exception e) {
            System.err.println("[Dhania] Failed to load config: " + e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            JsonObject mods = new JsonObject();
            for (ClientModule m : ModuleManager.ALL) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.enabled);
                o.addProperty("x", m.x);
                o.addProperty("y", m.y);
                mods.add(m.name, o);
            }
            root.add("modules", mods);
            root.add("waypoints", GSON.toJsonTree(WaypointManager.LIST));
            try (Writer w = Files.newBufferedWriter(FILE)) { GSON.toJson(root, w); }
        } catch (Exception e) {
            System.err.println("[Dhania] Failed to save config: " + e);
        }
    }
}
