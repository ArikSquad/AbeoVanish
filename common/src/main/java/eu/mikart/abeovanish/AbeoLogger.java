package eu.mikart.abeovanish;

import net.kyori.adventure.text.Component;

public interface AbeoLogger {
    void info(Component component);

    void info(String string);

    void warn(Component component);

    void warn(String string);
}
