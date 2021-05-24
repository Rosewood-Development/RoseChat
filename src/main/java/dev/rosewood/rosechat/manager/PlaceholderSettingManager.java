package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
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
    private Map<String, ChatReplacement> emojis;

    public PlaceholderSettingManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.placeholders = new HashMap<>();
        this.chatFormats = new HashMap<>();
        this.parsedFormats = new HashMap<>();
        this.tags = new HashMap<>();
        this.replacements = new HashMap<>();
        this.emojis = new HashMap<>();
    }

    @Override
    public void reload() {
        File placeholderFile = new File(this.rosePlugin.getDataFolder(), "placeholders.yml");
        if (!placeholderFile.exists()) this.rosePlugin.saveResource("placeholders.yml", false);

        File emojiFile = new File(this.rosePlugin.getDataFolder(), "emoji.yml");
        if (!emojiFile.exists()) this.rosePlugin.saveResource("emoji.yml", false);

        CommentedFileConfiguration placeholderConfiguration = CommentedFileConfiguration.loadConfiguration(placeholderFile);
        CommentedFileConfiguration emojiConfiguration = CommentedFileConfiguration.loadConfiguration(emojiFile);

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

            this.placeholders.put(id, placeholder);
        }

        // Emoji
        for (String id : emojiConfiguration.getKeys(false)) {
            String text = emojiConfiguration.getString(id + ".text");
            String replacement = emojiConfiguration.getString(id + ".replacement");
            String hover = emojiConfiguration.contains(id + ".hover") ? emojiConfiguration.getString(id + ".hover") : null;
            String font = emojiConfiguration.contains(id + ".font") ? emojiConfiguration.getString(id + ".font") : null;
            this.emojis.put(id, new ChatReplacement(id, text, replacement, hover, font));
        }

        // Replacements
        for (String id : Setting.CHAT_REPLACEMENTS.getSection().getKeys(false)) {
            CommentedConfigurationSection section = Setting.CHAT_REPLACEMENTS.getSection();
            String text = section.getString(id + ".text");
            String replacement = section.getString(id + ".replacement");
            String hover = section.contains(id + ".hover") ? section.getString(id + ".hover") : null;

            if (replacement.startsWith("{") && replacement.endsWith("}"))
                parseFormat("replacement-" + id, replacement);

            this.replacements.put(id, new ChatReplacement(id, text, replacement, hover));
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

            Tag tag = new Tag(id);
            tag.setPrefix(prefix);
            tag.setSuffix(suffix);
            tag.setTagOnlinePlayers(tagOnlinePlayers);
            tag.setMatchLength(matchLength);
            tag.setFormat(format);
            tag.setSound(sound);
            this.tags.put(id, tag);

            parseFormat("tag-" + id, format);
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

    // TODO: Conditional Placeholders
    private boolean loadConditionalPlaceholders(CommentedConfigurationSection config, Placeholder placeholder) {
        return false;
    }

    private void parseFormats() {
        for (String id : this.chatFormats.keySet()) {
            parseFormat(id, this.chatFormats.get(id));
        }
    }

    public void parseFormat(String id, String format) {
        String[] sections = format.split("[{}]");

        List<String> parsed = new ArrayList<>();

        for (String s : sections) {
            if (s.isEmpty()) continue;
            parsed.add(s);
        }

        this.parsedFormats.put(id, parsed);
    }

    public CustomPlaceholder getPlaceholder(String id) {
        return this.placeholders.get(id);
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

    public void addTag(Tag tag) {
        this.tags.put(tag.getId(), tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag.getId());
    }

    public Tag getTag(String id) {
        return this.tags.get(id);
    }

    public Map<String, Tag> getTags() {
        return this.tags;
    }

    public void addReplacement(ChatReplacement replacement) {
        this.replacements.put(replacement.getId(), replacement);
    }

    public void removeReplacement(ChatReplacement replacement) {
        this.replacements.remove(replacement.getId());
    }

    public ChatReplacement getReplacement(String id) {
        return this.replacements.get(id);
    }

    public Map<String, ChatReplacement> getReplacements() {
        return this.replacements;
    }

    public void addEmoji(ChatReplacement replacement) {
        this.emojis.put(replacement.getId(), replacement);
    }

    public void removeEmoji(ChatReplacement replacement) {
        this.emojis.remove(replacement.getId());
    }

    public ChatReplacement getEmoji(String id) {
        return this.emojis.get(id);
    }

    public Map<String, ChatReplacement> getEmojis() {
        return this.emojis;
    }
}
