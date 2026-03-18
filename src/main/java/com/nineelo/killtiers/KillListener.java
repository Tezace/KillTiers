package com.nineelo.killtiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillListener implements Listener {

    private final Main plugin;

    public KillListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        plugin.tierManager.updatePlayerStats(event.getPlayer().getUniqueId());
    }

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

            if (killerKills + 1 == 50) {
                if (!plugin.getConfig().getBoolean("abyssal_blade_awarded", false)) {
                    plugin.getConfig().set("abyssal_blade_awarded", true);
                    plugin.saveConfig();

                    org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(
                            org.bukkit.Material.NETHERITE_SWORD);
                    org.bukkit.inventory.meta.ItemMeta meta = sword.getItemMeta();
                    if (meta != null) {
                        meta.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                                .legacyAmpersand().deserialize("&d&o&lAbyssal Blade"));
                        org.bukkit.enchantments.Enchantment sharpness = org.bukkit.Registry.ENCHANTMENT
                                .get(org.bukkit.NamespacedKey.minecraft("sharpness"));
                        if (sharpness != null) {
                            meta.addEnchant(sharpness, 8, true);
                        }
                        sword.setItemMeta(meta);
                    }
                    killer.getInventory().addItem(sword);
                    org.bukkit.Bukkit.broadcast(net.kyori.adventure.text.Component.text(killer.getName()
                            + " is the first to reach the Limitless tier and has been awarded the Abyssal Blade!",
                            net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}