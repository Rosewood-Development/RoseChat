package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.placeholder.ConditionManager;
import dev.rosewood.rosechat.placeholder.CustomPlaceholder;
import dev.rosewood.rosechat.placeholder.DiscordEmbedPlaceholder;
import dev.rosewood.rosechat.placeholder.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderManager extends Manager {

    private final Map<String, CustomPlaceholder> placeholders;
    private final Map<String, String> chatFormats;
    private final Map<String, List<String>> parsedFormats;
    private DiscordEmbedPlaceholder discordEmbedPlaceholder;

    public PlaceholderManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.placeholders = new HashMap<>();
        this.chatFormats = new HashMap<>();
        this.parsedFormats = new HashMap<>();
    }

    @Override
    public void reload() {
        this.placeholders.clear();
        this.chatFormats.clear();
        this.parsedFormats.clear();
        this.discordEmbedPlaceholder = null;

        File placeholderFile = new File(this.rosePlugin.getDataFolder(), "custom-placeholders.yml");
        if (!placeholderFile.exists())
            this.rosePlugin.saveResource("custom-placeholders.yml", false);

        CommentedFileConfiguration placeholderConfiguration = CommentedFileConfiguration.loadConfiguration(placeholderFile);

        // Placeholders
        for (String id : placeholderConfiguration.getKeys(false)) {
            CustomPlaceholder placeholder = new CustomPlaceholder(id);

            ConfigurationSection placeholderSection = placeholderConfiguration.getConfigurationSection(id);
            if (placeholderSection == null)
                continue;

            if (Setting.MINECRAFT_TO_DISCORD_FORMAT.getString().contains(id)
                    && (placeholderSection.contains("title") || placeholderSection.contains("description"))) {
                this.discordEmbedPlaceholder = new DiscordEmbedPlaceholder(id);
                this.discordEmbedPlaceholder.parse(placeholderSection);
                continue;
            }

            for (String location : placeholderSection.getKeys(false)) {
                String conditionStr = placeholderSection.getString(location + ".condition");
                PlaceholderCondition condition = ConditionManager.getCondition(placeholderSection.getConfigurationSection(location), conditionStr).parseValues();
                placeholder.add(location, condition);
            }

            this.placeholders.put(id, placeholder);
        }

        // Formats
        for (String format : Setting.CHAT_FORMATS.getSection().getKeys(false)) {
            this.chatFormats.put(format, Setting.CHAT_FORMATS.getSection().getString(format));
        }

        parseFormats();
    }

    @Override
    public void disable() {

    }

    private void parseFormats() {
        for (String id : this.chatFormats.keySet()) {
            parseFormat(id, this.chatFormats.get(id));
        }
    }

    public void parseFormat(String id, String format) {
        this.chatFormats.put(id, format);
        String[] sections = format.split("[{}]");

        List<String> parsed = new ArrayList<>();

        for (String s : sections) {
            if (s.isEmpty())
                continue;

            parsed.add(s);
        }

        this.parsedFormats.put(id, parsed);
    }

    public CustomPlaceholder getPlaceholder(String id) {
        return this.placeholders.getOrDefault(id, null);
    }

    public Map<String, CustomPlaceholder> getPlaceholders() {
        return this.placeholders;
    }

    public String getChatFormat(String id) {
        return this.chatFormats.get(id);
    }

    public Map<String, String> getChatFormats() {
        return this.chatFormats;
    }

    public List<String> getParsedFormat(String id) {
        return this.parsedFormats.get(id);
    }

    public Map<String, List<String>> getParsedFormats() {
        return this.parsedFormats;
    }

    public DiscordEmbedPlaceholder getDiscordEmbedPlaceholder() {
        return this.discordEmbedPlaceholder;
    }

}
