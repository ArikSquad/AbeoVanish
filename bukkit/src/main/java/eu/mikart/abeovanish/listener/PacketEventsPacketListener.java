package eu.mikart.abeovanish.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.Player;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PacketEventsPacketListener implements PacketListener {
	private final IAbeo plugin;

	@Override
	public void onPacketSend(PacketSendEvent event) {
		org.bukkit.entity.Player user = event.getPlayer();
		if (event.getPacketType() != PacketType.Play.Server.TAB_COMPLETE)
			return;

		if(user.hasPermission("abeovanish.see")) {
			return;
		}

		WrapperPlayServerTabComplete wrapperPlayServerTabComplete = new WrapperPlayServerTabComplete(event);

		List<WrapperPlayServerTabComplete.CommandMatch> commands = wrapperPlayServerTabComplete.getCommandMatches();
		commands.forEach(commandMatch -> {
			String command = commandMatch.getText();
			String[] vanishedNames = plugin.getOnlineVanishedPlayers().stream()
					.map(Player::getName)
					.toArray(String[]::new);
			for (String name : vanishedNames) {
				if (command.contains(name)) {
					commandMatch.setText("");
					break;
				}
			}
		});
	}

}
