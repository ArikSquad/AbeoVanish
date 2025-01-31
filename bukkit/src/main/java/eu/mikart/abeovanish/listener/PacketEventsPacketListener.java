package eu.mikart.abeovanish.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PacketEventsPacketListener implements PacketListener {
    private final IAbeo plugin;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        org.bukkit.entity.Player user = event.getPlayer();

        if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            WrapperPlayClientChatMessage wrapperPlayClientChatMessage = new WrapperPlayClientChatMessage(event);
            Player player = BukkitPlayer.adapt(user);
            if (player.isVanished(plugin)) {
                if (!wrapperPlayClientChatMessage.getMessage().startsWith(plugin.getSettings().getVanishSettings().getBypassChatCharacter())) {
                    event.setCancelled(true);
                    plugin.getLocales().getLocale("vanished_chat_message", plugin.getSettings().getVanishSettings().getBypassChatCharacter()).ifPresent(player::sendMessage);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
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
        }
    }

}
