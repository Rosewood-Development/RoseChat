package dev.rosewood.rosechat.managers;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.Emote;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import dev.rosewood.rosechat.placeholders.ClickPlaceholder;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosechat.placeholders.HoverPlaceholder;
import dev.rosewood.rosechat.placeholders.TextPlaceholder;

import net.md_5.bungee.api.chat.ClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderManager {

    private YMLFile config;
    private Map<String, CustomPlaceholder> placeholders;
    private Map<String, String> formats;
    private Map<String, List<String>> parsedFormats;
    private Map<String, Tag> tags;
    private Map<String, Emote> emotes;

    public PlaceholderManager(RoseChat plugin) {
        this.config = plugin.getConfigFile();
        this.placeholders = new HashMap<>();
        formats = new HashMap<>();
        this.parsedFormats = new HashMap<>();
        this.tags = new HashMap<>();
        this.emotes = new HashMap<>();
        load();
    }

    public void load() {
        for (String id : config.getSection("emotes").getKeys(false)) {
            String text = config.getString("emotes." + id + ".text");
            String replacement = config.getString("emotes." + id + ".replacement");
            emotes.put(id, new Emote(id, text, replacement));
        }

        for (String id : config.getSection("tags").getKeys(false)) {
            String prefix = config.getString("tags." + id + ".prefix");
            boolean tagOnlinePlayers = config.getBoolean("tags." + id + ".tag-online-players");
            String format = config.getString("tags." + id + ".format");
            Sound sound;

            try {
                sound = Sound.valueOf(config.getString("tags." + id + ".sound"));
            } catch (Exception e) {
                sound = null;
            }

            Tag tag = new Tag(id).setPrefix(prefix).setTagOnlinePlayers(tagOnlinePlayers).setFormat(format).setSound(sound);
            tags.put(id, tag);
            parseFormat(id, format);
        }

        for (String id : config.getSection("custom-placeholders").getKeys(false)) {
            CustomPlaceholder placeholder = new CustomPlaceholder(id);
            TextPlaceholder textPlaceholder = new TextPlaceholder();
            HoverPlaceholder hoverPlaceholder = new HoverPlaceholder();
            ClickPlaceholder clickPlaceholder = new ClickPlaceholder();

            Map<String, String> textPlaceholders = new HashMap<>();
            for (String textGroup : config.getSection("custom-placeholders." + id + ".text").getKeys(false)) {
                String text = config.getString("custom-placeholders." + id + ".text." + textGroup);
                textPlaceholders.put(textGroup, text);
            }

            Map<String, List<String>> hoverPlaceholders = new HashMap<>();
            List<String> hoverLines;

            if (config.contains("custom-placeholders." + id + ".hover")) {
                for (String hoverGroup : config.getSection("custom-placeholders." + id + ".hover").getKeys(false)) {
                    hoverLines = config.getStringList("custom-placeholders." + id + ".hover." + hoverGroup);
                    hoverPlaceholders.put(hoverGroup, hoverLines);
                }
            }

            Map<String, ClickEvent> clickPlaceholders = new HashMap<>();
            ClickEvent clickEvent;

            if (config.contains("custom-placeholders." + id + ".click")) {
                for (String clickGroup : config.getSection("custom-placeholders." + id + ".click").getKeys(false)) {
                    String action = config.getString("custom-placeholders." + id + ".click." + clickGroup + ".action");
                    String value = config.getString("custom-placeholders." + id + ".click." + clickGroup + ".value");
                    clickEvent = new ClickEvent(ClickEvent.Action.valueOf(action), value);
                    clickPlaceholders.put(clickGroup, clickEvent);
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

        for (String format : config.getSection("chat-formats").getKeys(false)) {
            formats.put(format, config.getString("chat-formats." + format));
        }

        parseFormats();
    }

    private void parseFormats() {
        for (String id : formats.keySet()) {
            String format = formats.get(id);
            parseFormat(id, format);
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

    public Map<String, String> getFormats() {
        return formats;
    }

    public Map<String, List<String>> getParsedFormats() {
        return parsedFormats;
    }

    public Emote getEmote(String id) {
        return emotes.get(id);
    }

    public Map<String, Emote> getEmotes() {
        return emotes;
    }

    public Tag getTag(String id) {
        return tags.get(id);
    }

    public Map<String, Tag> getTags() {
        return tags;
    }
}
