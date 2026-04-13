package com.nineelo.killtiers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbyssalBladeListener implements Listener {

    private final Main plugin;
    private final NamespacedKey bladeKey;

    private static class ComboData {
        int combo;
        long lastHitTime;
        BukkitTask resetTask;

        ComboData(int combo, long lastHitTime, BukkitTask resetTask) {
            this.combo = combo;
            this.lastHitTime = lastHitTime;
            this.resetTask = resetTask;
        }
    }

    private final Map<UUID, ComboData> combos = new HashMap<>();

    public AbyssalBladeListener(Main plugin) {
        this.plugin = plugin;
        this.bladeKey = new NamespacedKey(plugin, "abyssal_blade");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;
        if (!(event.getEntity() instanceof LivingEntity target))
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(bladeKey, PersistentDataType.BYTE)) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        ComboData data = combos.get(uuid);

        if (data != null) {
            long diff = currentTime - data.lastHitTime;
            if (diff < 500) {
                // Not fully charged, reset combo
                cancelResetTask(data);
                combos.remove(uuid);
                return;
            } else if (diff > 1000) {
                // Too late, reset combo and start a new one
                cancelResetTask(data);
                data = null;
            }
        }

        int newCombo = data == null ? 1 : Math.min(data.combo + 1, 5);

        double multiplier = 1.0;
        int potionTier = 0;

        switch (newCombo) {
            case 1:
                multiplier = 1.05;
                potionTier = 0;
                break; // Level 1 (amplifier 0)
            case 2:
                multiplier = 1.10;
                potionTier = 0;
                break;
            case 3:
                multiplier = 1.15;
                potionTier = 1;
                break; // Level 2
            case 4:
                multiplier = 1.20;
                potionTier = 1;
                break;
            case 5:
                multiplier = 1.25;
                potionTier = 2;
                break; // Level 3
        }

        event.setDamage(event.getDamage() * multiplier);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, potionTier, true, true)); // 2 seconds
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, potionTier, true, true));

        final int finalNewCombo = newCombo;
        final double finalMultiplier = multiplier;

        if (data != null)
            cancelResetTask(data);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long elapsed = now - currentTime;
                double remainingSec = Math.max(0, 1.0 - (elapsed / 1000.0));

                if (elapsed > 1000 || !player.isOnline()) {
                    // Reset combo
                    combos.remove(uuid);
                    if (player.isOnline()) {
                        player.sendActionBar(Component.empty());
                    }
                    this.cancel();
                    return;
                }

                String actionBarText = String.format(
                        "§d§lCombo: x%d! §r| §a%.2fx Damage Boost §r| §bNext Timing: %.2fs",
                        finalNewCombo, finalMultiplier, remainingSec);
                player.sendActionBar(Component.text(actionBarText));
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick to update the action bar

        combos.put(uuid, new ComboData(newCombo, currentTime, task));
    }

    private void cancelResetTask(ComboData data) {
        if (data.resetTask != null && !data.resetTask.isCancelled()) {
            data.resetTask.cancel();
        }
    }
}
