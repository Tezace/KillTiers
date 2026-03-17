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

    public PlayerData(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.cfg = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    public void loadAll() {
        for (String key : cfg.getKeys(false)) {
            kills.put(UUID.fromString(key), cfg.getInt(key + ".kills"));
        }
    }

    public void saveAll() {
        kills.forEach((uuid, killCount) -> {
            cfg.set(uuid.toString() + ".kills", killCount);
        });
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
}