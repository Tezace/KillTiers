package com.nineelo.killtiers;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Main extends JavaPlugin {

    public static Main instance;
    public PlayerData data;
    public TierManager tierManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        data = new PlayerData(this);
        tierManager = new TierManager(this);

        getServer().getPluginManager().registerEvents(new KillListener(this), this);

        getCommand("toppoints").setExecutor((sender, cmd, label, args) -> {
            tierManager.showLeaderboard(sender);
            return true;
        });
    }

    @Override
    public void onDisable() {
        data.saveAll();
    }
}