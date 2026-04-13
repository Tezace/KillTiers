package com.nineelo.killtiers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {

    private final Main plugin;
    private final File file;
    private final FileConfiguration cfg;
    public HashMap<UUID, Integer> kills = new HashMap<>();
    private boolean abyssalBladeClaimed = false;

    public PlayerData(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.cfg = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    public void loadAll() {
        for (String key : cfg.getKeys(false)) {
            if (key.equals("global")) continue;
            UUID uuid = UUID.fromString(key);
            kills.put(uuid, cfg.getInt(key + ".kills"));
        }
        abyssalBladeClaimed = cfg.getBoolean("global.abyssal_blade_claimed", false);
    }

    public void saveAll() {
        kills.forEach((uuid, killCount) -> {
            cfg.set(uuid.toString() + ".kills", killCount);
        });
        cfg.set("global.abyssal_blade_claimed", abyssalBladeClaimed);
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getKills(UUID uuid) {
        return kills.getOrDefault(uuid, 0);
    }

    public void setKills(UUID uuid, int amount) {
        kills.put(uuid, amount);
    }

    public boolean isAbyssalBladeClaimed() {
        return abyssalBladeClaimed;
    }

    public void setAbyssalBladeClaimed(boolean claimed) {
        this.abyssalBladeClaimed = claimed;
    }
}
