package eu.mikart.abeovanish.commands;

import dev.jorel.commandapi.CommandAPICommand;
import eu.mikart.abeovanish.IAbeo;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AbeoCommand {
    private final IAbeo plugin;

    public AbeoCommand(IAbeo plugin) {
        this.plugin = plugin;

        CommandAPICommand version = new CommandAPICommand("version")
                .withPermission("abeovanish.about")
                .executes((player, args) -> {
                    sendVersion(player);
                });

        CommandAPICommand reload = new CommandAPICommand("reload")
                .withPermission("abeovanish.reload")
                .executes((player, args) -> {
                    long startTime = System.currentTimeMillis();
                    plugin.reload();
                    long endTime = System.currentTimeMillis();

                    plugin.getLocales().getLocale("plugin_reload", String.valueOf(endTime - startTime)).ifPresent(player::sendMessage);
                });

        new CommandAPICommand("abeo")
                .withPermission("abeovanish.about")
                .withSubcommands(version, reload)
                .executes((player, args) -> {
                    sendVersion(player);
                }).register();
    }

    private void sendVersion(Audience audience) {
        audience.sendMessage(Component.text("AbeoVanish v" + plugin.getVersion()).color(NamedTextColor.GREEN));
    }

}
