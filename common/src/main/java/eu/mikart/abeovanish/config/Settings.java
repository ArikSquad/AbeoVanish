package eu.mikart.abeovanish.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
@SuppressWarnings("FieldMayBeFinal")
public class Settings {

	protected static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃       AbeoVanish Config      ┃
            ┃    Developed by ArikSquad    ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Information: link here
            ┣╸ Config Help: link here
            ┗╸ Documentation: link here""";

	@Comment("Locale of the default language file to use. Docs: ")
	private String language = Locales.DEFAULT_LOCALE;

	@Comment("Whether to enable debug mode. This will print additional information to the console.")
	private boolean debug = false;

	@Comment("Whether to enable the plugin's update checker.")
	private boolean updateChecker = true;

}
