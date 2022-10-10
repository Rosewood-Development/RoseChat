package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.placeholders.ConditionManager;
import dev.rosewood.rosechat.placeholders.DiscordPlaceholder;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;
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

    private final Map<String, RoseChatPlaceholder> placeholders;
    private final Map<String, String> chatFormats;
    private final Map<String, List<String>> parsedFormats;
    private DiscordPlaceholder discordPlaceholder;

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

        File placeholderFile = new File(this.rosePlugin.getDataFolder(), "placeholders.yml");
        if (!placeholderFile.exists()) this.rosePlugin.saveResource("placeholders.yml", false);

        CommentedFileConfiguration placeholderConfiguration = CommentedFileConfiguration.loadConfiguration(placeholderFile);

        // Placeholders
        for (String id : placeholderConfiguration.getKeys(false)) {
            if (Setting.MINECRAFT_TO_DISCORD_FORMAT.getString().contains(id)) {
                if (placeholderConfiguration.contains(id)) parseDiscordFormat(id, placeholderConfiguration.getConfigurationSection(id));
                continue;
            }

            RoseChatPlaceholder placeholder = new RoseChatPlaceholder(id);

            if (placeholderConfiguration.contains(id + ".text")) {
                String conditionStr = placeholderConfiguration.contains(id + ".text.condition") ? placeholderConfiguration.getString(id + ".text.condition") : null;
                PlaceholderCondition condition = ConditionManager.getCondition(placeholderConfiguration.getConfigurationSection(id + ".text"), conditionStr).parseValues();
                placeholder.setText(condition);
            }

            if (placeholderConfiguration.contains(id + ".hover")) {
                String conditionStr = placeholderConfiguration.contains(id + ".hover.condition") ? placeholderConfiguration.getString(id + ".hover.condition") : null;
                PlaceholderCondition condition = ConditionManager.getCondition(placeholderConfiguration.getConfigurationSection(id + ".hover"), conditionStr).parseValues();
                placeholder.setHover(condition);
            }

            if (placeholderConfiguration.contains(id + ".click")) {
                String conditionStr = placeholderConfiguration.contains(id + ".click.condition") ? placeholderConfiguration.getString(id + ".click.condition") : null;
                PlaceholderCondition condition = ConditionManager.getCondition(placeholderConfiguration.getConfigurationSection(id + ".click"), conditionStr).parseValues();
                placeholder.setClick(condition);
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

    private void parseDiscordFormat(String format, ConfigurationSection section) {
        DiscordPlaceholder discordPlaceholder = new DiscordPlaceholder(format);

        for (String embedInfo : section.getKeys(false)) {
            String condition = section.contains(embedInfo + ".condition") ? section.getString(embedInfo + ".condition") : null;
            PlaceholderCondition placeholder = ConditionManager.getCondition(section.getConfigurationSection(embedInfo), condition).parseValues();
            discordPlaceholder.addPlaceholder(embedInfo, placeholder);
        }

        this.discordPlaceholder = discordPlaceholder;
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
            if (s.isEmpty()) continue;
            parsed.add(s);
        }

        this.parsedFormats.put(id, parsed);
    }

    public RoseChatPlaceholder getPlaceholder(String id) {
        return this.placeholders.get(id);
    }

    public Map<String, RoseChatPlaceholder> getPlaceholders() {
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

    public DiscordPlaceholder getDiscordPlaceholder() {
        return this.discordPlaceholder;
    }

}
