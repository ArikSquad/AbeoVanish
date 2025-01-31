package eu.mikart.abeovanish;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PaperLogger implements AbeoLogger {
    private final IAbeo plugin;
    private final Component prefix = Component.text("[AbeoVanish]");

    public PaperLogger(IAbeo plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(Component component) {
        plugin.getConsole().sendMessage(prefix.appendSpace().append(component));
    }

    @Override
    public void info(String string) {
        this.info(Component.text(string));
    }

    @Override
    public void warn(String string) {
        this.warn(Component.text(string));
    }

    @Override
    public void warn(Component component) {
        plugin.getConsole().sendMessage(
                Component.text("[WARN]")
                        .append(prefix.appendSpace().append(component))
                        .color(NamedTextColor.YELLOW));
    }
}
