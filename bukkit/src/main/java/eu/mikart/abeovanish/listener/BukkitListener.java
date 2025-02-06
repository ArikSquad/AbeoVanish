package eu.mikart.abeovanish.listener;

import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class BukkitListener implements Listener {

    private final IAbeo plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;

        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (player.isVanished(plugin)) {
            // Early vanish if possible
            plugin.getVanishStateManager().setVanished(player, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (player.isVanished(plugin)) {
            if (event.getPlayer().getMetadata("vanished").isEmpty()
                    || !event.getPlayer().getMetadata("vanished").getFirst().asBoolean()) {
                plugin.getVanishStateManager().setVanished(player, true);
            }
            // On join and the player is vanished, set the joinMessage to null, so it won't be sent to the players
            event.joinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (player.isVanished(plugin)) {
            // On quit and the player is vanished, set the quitMessage to null, so it won't be sent to the players
            event.quitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (player.isVanished(plugin)) {
            // Send a death message to all players with the permission abeovanish.see
            for (Player p : plugin.getOnlinePlayers()) {
                org.bukkit.entity.Player onlineBukkitPlayer = ((BukkitPlayer) p).getBukkitPlayer();
                if (!onlineBukkitPlayer.hasPermission("abeovanish.see")) {
                    continue;
                }
                if (event.deathMessage() != null) {
                    p.sendMessage(Objects.requireNonNull(event.deathMessage()));
                } else {
                    plugin.getLocales().getLocale("vanished_death", player.getName()).ifPresent(p::sendMessage);
                }
            }
            // When a vanished player dies, don't send an public announcement
            event.deathMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player p) {
            Player player = BukkitPlayer.adapt(p);
            if (player.isVanished(plugin)) {
                // Cancel the damage event if the player is vanished
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCropTrample(PlayerInteractEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (player.isVanished(plugin)) {
            // Check if the setting is enabled for crop trampling to be disabled
            if (!plugin.getSettings().getVanishSettings().isDisableCropTrampling()) return;

            // Check if the action is physical
            if (event.getAction() != Action.PHYSICAL) return;

            // Check if the block is soil or farmland and cancel the event
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().toString().matches("SOIL|FARMLAND"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        // Slight amount of checks
        if (!event.getRightClicked().getType().equals(EntityType.PLAYER)) {
            return;
        }

        if (!plugin.getSettings().getHookSettings().getOpenInv().isEnabled()) {
            return;
        }

        if (plugin.getSettings().getHookSettings().getOpenInv().isRequireSpectatorToOpenInv() && event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        if (plugin.getSettings().getHookSettings().getOpenInv().isRequireSneakingToOpenInv()) {
            if (!event.getPlayer().isSneaking()) {
                return;
            }
        }

        if (!event.getPlayer().hasPermission("abeovanish.openinv")) {
            return;
        }

        if (plugin.getOpenInv() == null) {
            return;
        }

        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (!player.isVanished(plugin)) {
            return;
        }

        event.setCancelled(true);
        org.bukkit.entity.Player target = (org.bukkit.entity.Player) event.getRightClicked();

        try {
            plugin.getOpenInv().openInventory(player, plugin.getOpenInv().getInventory(BukkitPlayer.adapt(target), true));
        } catch (InstantiationException e) {
            plugin.getAdventureLogger().warn("Failed to open inventory for " + target.getName());
        }
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (player.isVanished(plugin)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (!player.isVanished(plugin)) {
            return;
        }

        if (plugin.getSettings().getVanishSettings().isReappearOnWorldChange()) {
            if (!event.getPlayer().hasPermission("abeovanish.bypass.worldchange")) {
                plugin.getVanishStateManager().setVanished(player, false);
            }
        }
    }

    // This was quite hard to do in packet-level stuff so I think the best way it to just leave it here.
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (!player.isVanished(plugin)) {
            return;
        }

        if (event.getAction().equals(Action.PHYSICAL) && event.getClickedBlock() != null) {
            if (!plugin.getSettings().getVanishSettings().isDisablePressurePlates()) {
                return;
            }
            String material = event.getClickedBlock().getType().toString();
            List<String> disallowedMaterials = Arrays.asList("STONE_PLATE", "GOLD_PLATE", "IRON_PLATE",
                    "WOOD_PLATE"/* <- LEGACY*/, "TRIPWIRE", "PRESSURE_PLATE");
            for (String disallowedMaterial : disallowedMaterials)
                if (material.equals(disallowedMaterial) || material.contains(disallowedMaterial)) {
                    event.setCancelled(true);
                }
        }
    }

    // TODO: move to packet listener. Shouldn't be to hard,
    //  just that I have no idea which packet is the most effective to be listened to.
    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getSettings().getExperimental().isUseBlockBreakPacket()) return;
        Player player = BukkitPlayer.adapt(event.getPlayer());
        if (!player.isVanished(plugin)) {
            return;
        }

        if (plugin.getSettings().getVanishSettings().isDisableBlockBreaking()) {
            if (!event.getPlayer().hasPermission("abeovanish.bypass.blockbreak")) {
                event.setCancelled(true);
                plugin.getLocales().getLocale("vanished_blockbreak").ifPresent(player::sendMessage);
            }
        }
    }
}
