package eu.mikart.abeovanish.listener.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WorldPacketListener implements PacketListener {

    private final IAbeo plugin;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        org.bukkit.entity.Player user = event.getPlayer();
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            Player player = BukkitPlayer.adapt(user);
            if (!player.isVanished(plugin)) {
                return;
            }

            if (plugin.getSettings().getVanishSettings().isDisableBlockPlacing()) {
                if (!user.hasPermission("abeovanish.bypass.blockplace")) {
                    event.setCancelled(true);
                    plugin.getLocales().getLocale("vanished_blockplace").ifPresent(player::sendMessage);
                }
            }
        } else if (plugin.getSettings().getExperimental().isUseBlockBreakPacket() && event.getPacketType().equals(PacketType.Play.Client.PLAYER_DIGGING)) {
            Player player = BukkitPlayer.adapt(user);
            if (!player.isVanished(plugin)) {
                return;
            }

            if (plugin.getSettings().getVanishSettings().isDisableBlockBreaking()) {
                if (!user.hasPermission("abeovanish.bypass.blockbreak")) {
                    event.setCancelled(true);
                    plugin.getLocales().getLocale("vanished_blockbreak").ifPresent(player::sendMessage);
                }
            }
        }
    }

}
