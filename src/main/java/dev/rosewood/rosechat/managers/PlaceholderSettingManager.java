package dev.rosewood.rosechat.managers;

import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.managers.ConfigurationManager.Setting;
import dev.rosewood.rosechat.placeholders.ClickPlaceholder;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosechat.placeholders.HoverPlaceholder;
import dev.rosewood.rosechat.placeholders.Placeholder;
import dev.rosewood.rosechat.placeholders.TextPlaceholder;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Sound;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderSettingManager extends Manager {

    private Map<String, CustomPlaceholder> placeholders;
    private Map<String, String> chatFormats;
    private Map<String, List<String>> parsedFormats;
    private Map<String, Tag> tags;
    private Map<String, ChatReplacement> replacements;

    public PlaceholderSettingManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.placeholders = new HashMap<>();
        this.chatFormats = new HashMap<>();
        this.parsedFormats = new HashMap<>();
        this.tags = new HashMap<>();
        this.replacements = new HashMap<>();
    }

    @Override
    public void reload() {
        File placeholderFile = new File(this.rosePlugin.getDataFolder(), "placeholders.yml");
        if (!placeholderFile.exists()) this.rosePlugin.saveResource("placeholders.yml", false);

        CommentedFileConfiguration placeholderConfiguration = CommentedFileConfiguration.loadConfiguration(placeholderFile);

        // Placeholders
        for (String id : placeholderConfiguration.getKeys(false)) {
            CustomPlaceholder placeholder = new CustomPlaceholder(id);
            TextPlaceholder textPlaceholder = new TextPlaceholder();
            HoverPlaceholder hoverPlaceholder = new HoverPlaceholder();
            ClickPlaceholder clickPlaceholder = new ClickPlaceholder();

            loadConditionalPlaceholders(placeholderConfiguration.getConfigurationSection(id), textPlaceholder);
            loadConditionalPlaceholders(placeholderConfiguration.getConfigurationSection(id), hoverPlaceholder);
            loadConditionalPlaceholders(placeholderConfiguration.getConfigurationSection(id), clickPlaceholder);


            Map<String, String> textPlaceholders = new HashMap<>();
            for (String textGroup : placeholderConfiguration.getConfigurationSection(id + ".text").getKeys(false)) {
                textPlaceholders.put(textGroup, placeholderConfiguration.getString(id + ".text." + textGroup));
            }

            Map<String, List<String>> hoverPlaceholders = new HashMap<>();
            if (placeholderConfiguration.contains(id + ".hover")) {
                for (String hoverGroup : placeholderConfiguration.getConfigurationSection(id + ".hover").getKeys(false))
                    hoverPlaceholders.put(hoverGroup, placeholderConfiguration.getStringList(id + ".hover." + hoverGroup));
            }

            Map<String, ClickEvent> clickPlaceholders = new HashMap<>();
            if (placeholderConfiguration.contains(id + ".click")) {
                for (String clickGroup : placeholderConfiguration.getConfigurationSection(id + ".click").getKeys(false)) {
                    String action = placeholderConfiguration.getString(id + ".click." + clickGroup + ".action");
                    String value = placeholderConfiguration.getString(id + ".click." + clickGroup + ".value");
                    clickPlaceholders.put(clickGroup, new ClickEvent(ClickEvent.Action.valueOf(action), value));
                }
            }

            textPlaceholder.setGroups(textPlaceholders);
            hoverPlaceholder.setGroups(hoverPlaceholders);
            clickPlaceholder.setGroups(clickPlaceholders);

            placeholder.setText(textPlaceholder);
            if (!hoverPlaceholders.isEmpty()) placeholder.setHover(hoverPlaceholder);
            if (!clickPlaceholders.isEmpty()) placeholder.setClick(clickPlaceholder);

            placeholders.put(id, placeholder);
        }

        // Replacements
        for (String id : Setting.CHAT_REPLACEMENTS.getSection().getKeys(false)) {
            CommentedConfigurationSection section = Setting.CHAT_REPLACEMENTS.getSection();
            String text = section.getString(id + ".text");
            String replacement = section.getString(id + ".replacement");
            String hover = section.contains(id + ".hover") ? section.getString(id + ".hover") : null;

            if (replacement.startsWith("{") && replacement.endsWith("}"))
                parseFormat("replacement-" + id, replacement);

            replacements.put(id, new ChatReplacement(id, text, replacement, hover));
        }

        // Tags
        for (String id : Setting.TAGS.getSection().getKeys(false)) {
            CommentedConfigurationSection section = Setting.TAGS.getSection();
            String prefix = section.getString(id + ".prefix");
            String suffix = section.getString(id + ".suffix");
            boolean tagOnlinePlayers = section.getBoolean(id + ".tag-online-players");
            boolean matchLength = section.getBoolean(id + ".match-length");
            String format = section.getString(id + ".format").replace("{", "").replace("}", "");
            Sound sound;

            try {
                sound = Sound.valueOf(section.getString(id + ".sound"));
            } catch (Exception e) {
                sound = null;
            }

            Tag tag = new Tag(id).setPrefix(prefix).setSuffix(suffix)
                    .setTagOnlinePlayers(tagOnlinePlayers).setMatchLength(matchLength)
                    .setFormat(format).setSound(sound);
            tags.put(id, tag);
            parseFormat("tag-" + id, format);
        }

        // Formats
        for (String format : Setting.CHAT_FORMATS.getSection().getKeys(false)) {
            chatFormats.put(format, Setting.CHAT_FORMATS.getSection().getString(format));
        }

        parseFormats();
    }

    @Override
    public void disable() {

    }

    // TODO: Conditional Placeholders
    private boolean loadConditionalPlaceholders(CommentedConfigurationSection config, Placeholder placeholder) {
        return false;
    }

    private void parseFormats() {
        for (String id : chatFormats.keySet()) {
            parseFormat(id, chatFormats.get(id));
        }
    }

    public void parseFormat(String id, String format) {
        String[] sections = format.split("[{}]");

        List<String> parsed = new ArrayList<>();

        for (String s : sections) {
            if (s.isEmpty()) continue;
            parsed.add(s);
        }

        parsedFormats.put(id, parsed);
    }

    public CustomPlaceholder getPlaceholder(String id) {
        return placeholders.get(id);
    }

    public Map<String, CustomPlaceholder> getPlaceholders() {
        return placeholders;
    }

    public String getChatFormat(String id) {
        return chatFormats.get(id);
    }

    public Map<String, String> getChatFormats() {
        return chatFormats;
    }

    public List<String> getParsedFormat(String id) {
        return parsedFormats.get(id);
    }

    public Map<String, List<String>> getParsedFormats() {
        return parsedFormats;
    }

    public Tag getTag(String id) {
        return tags.get(id);
    }

    public Map<String, Tag> getTags() {
        return tags;
    }

    public ChatReplacement getReplacement(String id) {
        return replacements.get(id);
    }

    public Map<String, ChatReplacement> getReplacements() {
        return replacements;
    }
}
