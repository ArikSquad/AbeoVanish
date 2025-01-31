package eu.mikart.abeovanish.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import eu.mikart.abeovanish.IAbeo;
import eu.mikart.abeovanish.user.BukkitPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VanishCommand {
    private final IAbeo plugin;

    public VanishCommand(IAbeo plugin) {
        this.plugin = plugin;

        List<Argument<?>> settingsArguments = new ArrayList<>();
        settingsArguments.add(new StringArgument("setting")
                .replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info ->
                        new IStringTooltip[]{
                                StringTooltip.ofString("pickup", "Allow to pick up items"),
                                StringTooltip.ofString("damage", "Enable the possibility to damage other players"),
                        }
                ))
        );

        // I tried with arguments, but we get best results with subcommands for this case
        // When we expect to get the best autocompletion results
        CommandAPICommand onCommand = new CommandAPICommand("on")
                .withPermission("abeovanish.vanish")
                .executesPlayer((player, args) -> {
                    setVanish(player, true);
                });

        CommandAPICommand offCommand = new CommandAPICommand("off")
                .withPermission("abeovanish.vanish")
                .executesPlayer((player, args) -> {
                    setVanish(player, false);
                });

        CommandAPICommand setOtherPlayerVanishCommand = new CommandAPICommand("set")
                .withPermission("abeovanish.vanish.others")
                .withArguments(new StringArgument("targetPlayer"))
                .withArguments(new StringArgument("vanishState").replaceSuggestions(ArgumentSuggestions.strings("on", "off")))
                .executesPlayer((player, args) -> {
                    String targetName = (String) args.get("targetPlayer");
                    String stateArg = (String) args.get("vanishState");

                    BukkitPlayer targetPlayer = plugin.getOnlinePlayers().stream()
                            .filter(p -> ((BukkitPlayer) p).getBukkitPlayer().getName().equalsIgnoreCase(targetName))
                            .map(p -> (BukkitPlayer) p)
                            .findFirst()
                            .orElse(null);

                    if (targetPlayer == null) {
                        // Pelaajaa ei löytynyt
                        plugin.getLocales().getLocale("player_not_found", targetName).ifPresent(player::sendMessage);
                        return;
                    }

                    boolean vanishState = stateArg.equalsIgnoreCase("on");

                    plugin.getVanishStateManager().setVanished(targetPlayer, vanishState);
                    plugin.getLocales().getLocale("vanish_" + (vanishState ? "on" : "off"), targetPlayer.getBukkitPlayer().getName())
                            .ifPresent(player::sendMessage);
                    plugin.getLocales().getLocale("vanished_by_other", stateArg, player.getName())
                            .ifPresent(targetPlayer.getBukkitPlayer()::sendMessage);
                });

        CommandAPICommand setting = new CommandAPICommand("setting")
                .withArguments(settingsArguments)
                .executesPlayer((player, args) -> {
                    String s = (String) args.get("setting");
                    if (s == null) {
                        plugin.getLocales().getLocale("invalid_vanish_setting").ifPresent(player::sendMessage);
                        return;
                    }

                    player.sendMessage(Component.text(s).color(NamedTextColor.WHITE)
                            .appendSpace()
                            .append(Component.text("NOT IMPLEMENTED").color(NamedTextColor.BLUE))
                    );
                });

        new CommandAPICommand("vanish")
                .withAliases("v")
                .withPermission("abeovanish.vanish")
                .withSubcommands(setting, onCommand, offCommand, setOtherPlayerVanishCommand)
                .executesPlayer((player, args) -> {
                    handleVanish(player);
                })
                .register();
    }

    private void handleVanish(Player player) {
        boolean toggle = plugin.getDatabase().getVanishState(player.getUniqueId());
        setVanish(player, !toggle);
    }

    private void setVanish(Player player, boolean state) {
        plugin.getVanishStateManager().setVanished(BukkitPlayer.adapt(player), state);

        for (eu.mikart.abeovanish.user.Player p : plugin.getOnlinePlayers()) {
            Player onlinePlayer = ((BukkitPlayer) p).getBukkitPlayer();

            if (plugin.getSettings().isSendJoinQuitMessages()) {
                plugin.getLocales().getLocale("fake_" + (state ? "quit" : "join"), player.getName()).ifPresent(onlinePlayer::sendMessage);
            }
        }

        plugin.getLocales().getLocale("vanish_" + (state ? "on" : "off")).ifPresent(player::sendMessage);
    }

}
