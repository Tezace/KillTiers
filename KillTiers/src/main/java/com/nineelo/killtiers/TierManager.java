package com.nineelo.killtiers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TierManager {

    private final Main plugin;
    private final Scoreboard board;
    private final Objective sidebar;

    public TierManager(Main plugin) {
        this.plugin = plugin;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        // always use a new custom scoreboard so plugin doesn't conflict
        board = Objects.requireNonNull(manager).getNewScoreboard();

        // register or get objective
        if (board.getObjective("TopKills") == null) {
            sidebar = board.registerNewObjective(
                    "TopKills", Criteria.DUMMY, Component.text("Top Points")
            );
        } else {
            sidebar = board.getObjective("TopKills");
        }
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void updatePlayerStats(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        int kills = plugin.data.getKills(uuid);
        Tier tier = getTier(kills);

        // Apply health
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(20.0 * tier.multiplier);
        }

        // Apply attack damage
        AttributeInstance attackAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(1.0 * tier.multiplier);
        }

        // Team Setup
        String teamName = "tier_" + tier.name.toLowerCase();
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        // Set prefix safely (max 16 chars typical limit)
        team.prefix(Component.text("[" + tier.name + "] ", tier.color));
        team.addEntry(player.getName());

        // Ensure player views this scoreboard
        player.setScoreboard(board);

        // Reset old score line if exists
        sidebar.getScore(player.getName()).setScore(kills);
    }

    public Tier getTier(int kills) {
        if (kills >= 50) {
            int extra = (kills - 50) / 10;
            return new Tier("Limitless+" + extra, 2.0 + 0.25 * extra, NamedTextColor.DARK_PURPLE);
        } else if (kills >= 30) return new Tier("Celestial", 1.8, NamedTextColor.GOLD);
        else if (kills >= 25) return new Tier("Insane", 1.7, NamedTextColor.RED);
        else if (kills >= 20) return new Tier("Legendary", 1.6, NamedTextColor.DARK_AQUA);
        else if (kills >= 15) return new Tier("Grandmaster", 1.5, NamedTextColor.LIGHT_PURPLE);
        else if (kills >= 10) return new Tier("Master", 1.4, NamedTextColor.GREEN);
        else if (kills >= 7 ) return new Tier("Ace", 1.3, NamedTextColor.AQUA);
        else if (kills >= 5 ) return new Tier("Specialist", 1.2, NamedTextColor.YELLOW);
        else if (kills >= 3 ) return new Tier("Cadet", 1.1, NamedTextColor.GRAY);
        else if (kills >= 1 ) return new Tier("Novice", 1.05, NamedTextColor.WHITE);
        return new Tier("Rookie", 1.0, NamedTextColor.DARK_GRAY);
    }

    public void showLeaderboard(org.bukkit.command.CommandSender sender) {
        sender.sendMessage("Top Kill Leaderboard:");
        List<Map.Entry<String, Integer>> list = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            list.add(Map.entry(p.getName(), plugin.data.getKills(p.getUniqueId())));
        }

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < Math.min(list.size(), 10); i++) {
            sender.sendMessage((i + 1) + ". " + list.get(i).getKey()
                    + " - " + list.get(i).getValue());
        }
    }

    private record Tier(String name, double multiplier, NamedTextColor color) {}
}