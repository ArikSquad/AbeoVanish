package eu.mikart.abeovanish;

import eu.mikart.abeovanish.config.Locales;
import eu.mikart.abeovanish.config.Settings;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.logging.Logger;

@Setter
@Getter
public final class AbeoBukkit extends JavaPlugin implements IAbeo {
    private Settings settings;
    private Locales locales;

    @Override
    public void onEnable() {
        this.loadConfig();
    }

    @Override
    @NotNull
    public Path getConfigDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    @NotNull
    public AbeoBukkit getPlugin() {
        return this;
    }

    @Override
    @NotNull
    public Logger getLogger() {
        return super.getLogger();
    }
}
