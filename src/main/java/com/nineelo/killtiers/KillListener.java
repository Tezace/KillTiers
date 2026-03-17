package com.nineelo.killtiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillListener implements Listener {

    private final Main plugin;

    public KillListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            var killer = event.getEntity().getKiller();
            var victim = event.getEntity();

            int killerKills = plugin.data.getKills(killer.getUniqueId());
            plugin.data.setKills(killer.getUniqueId(), killerKills + 1);

            int victimKills = plugin.data.getKills(victim.getUniqueId());
            plugin.data.setKills(victim.getUniqueId(), Math.max(victimKills - 2, 0));

            plugin.tierManager.updatePlayerStats(killer.getUniqueId());
            plugin.tierManager.updatePlayerStats(victim.getUniqueId());
        }
    }
}