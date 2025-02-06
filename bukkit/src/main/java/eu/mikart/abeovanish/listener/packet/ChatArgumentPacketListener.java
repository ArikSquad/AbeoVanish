package eu.mikart.abeovanish.listener.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class ChatArgumentPacketListener implements PacketListener {

    private final IAbeo plugin;

    // Not sure if this is the correct way to handle this
    @Override
    public void onPacketSend(PacketSendEvent event) {
        org.bukkit.entity.Player user = event.getPlayer();
        if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
            if (user.hasPermission("abeovanish.see")) {
                return;
            }

            WrapperPlayServerTabComplete wrapperPlayServerTabComplete = new WrapperPlayServerTabComplete(event);
            wrapperPlayServerTabComplete.setCommandMatches(getCommandMatches(wrapperPlayServerTabComplete));
        }
    }

    private @NotNull List<WrapperPlayServerTabComplete.CommandMatch> getCommandMatches(WrapperPlayServerTabComplete wrapperPlayServerTabComplete) {
        List<WrapperPlayServerTabComplete.CommandMatch> commands = wrapperPlayServerTabComplete.getCommandMatches();
        commands.forEach(commandMatch -> {
            String command = commandMatch.getText().toLowerCase(Locale.ENGLISH);
            String[] vanishedNames = plugin.getOnlineVanishedPlayers().stream()
                    .map(Player::getName)
                    .map(name -> name.toLowerCase(Locale.ENGLISH))
                    .toArray(String[]::new);
            for (String name : vanishedNames) {
                if (command.contains(name)) {
                    commands.remove(commandMatch);
                    break;
                }
            }
        });
        return commands;
    }
}
