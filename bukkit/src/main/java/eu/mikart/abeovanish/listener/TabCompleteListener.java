package eu.mikart.abeovanish.listener;

import eu.mikart.abeovanish.IAbeo;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TabCompleteListener implements Listener {
    private final IAbeo plugin;

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) return;
        Set<String> vanishedNames = plugin.getOnlineVanishedPlayers().stream()
                .map(eu.mikart.abeovanish.user.Player::getName)
                .map(name -> name.toLowerCase(Locale.ENGLISH))
                .collect(Collectors.toSet());

        Iterator<String> it = event.getCompletions().iterator();
        while (it.hasNext()) {
            String completion = it.next();
            boolean allowedCompletion = !vanishedNames.contains(completion.toLowerCase(Locale.ENGLISH));
            if (!allowedCompletion) {
                it.remove();
            }
        }
    }
}
