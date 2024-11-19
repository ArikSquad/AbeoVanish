package eu.mikart.abeovanish;

import eu.mikart.abeovanish.config.ConfigProvider;

import java.util.logging.Logger;

public interface IAbeo extends ConfigProvider {

    Logger getLogger();

}