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
        getServer().getPluginManager().registerEvents(new AbyssalBladeListener(this), this);
        getCommand("topkills").setExecutor((sender, cmd, label, args) -> {
            tierManager.showLeaderboard(sender);
            return true;
        });

        getCommand("givekills").setExecutor((sender, cmd, label, args) -> {
            if (!sender.isOp()) {
                sender.sendMessage(net.kyori.adventure.text.Component.text("You do not have permission to use this command.", net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
            if (args.length != 2) {
                sender.sendMessage(net.kyori.adventure.text.Component.text("Usage: /givekills <player> <amount>", net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
            org.bukkit.entity.Player target = getServer().getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(net.kyori.adventure.text.Component.text("Player not found.", net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
            try {
                int amount = Integer.parseInt(args[1]);
                int currentKills = data.getKills(target.getUniqueId());
                data.setKills(target.getUniqueId(), currentKills + amount);
                tierManager.updatePlayerStats(target.getUniqueId());
                sender.sendMessage(net.kyori.adventure.text.Component.text("Gave " + amount + " kills to " + target.getName(), net.kyori.adventure.text.format.NamedTextColor.GREEN));
            } catch (NumberFormatException e) {
                sender.sendMessage(net.kyori.adventure.text.Component.text("Invalid amount.", net.kyori.adventure.text.format.NamedTextColor.RED));
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
        data.saveAll();
    }
}