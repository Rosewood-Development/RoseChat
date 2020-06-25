package dev.rosewood.rosechat.placeholders;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;

public class PlaceholderManager {

    private YMLFile config;
    private Map<String, CustomPlaceholder> placeholders;
    private Map<String, String> formats;
    private Map<String, List<String>> parsedFormats;

    public PlaceholderManager() {
        this.config = RoseChat.getInstance().getConfigFile();
        this.placeholders = new HashMap<>();
        formats = new HashMap<>();
        this.parsedFormats = new HashMap<>();
        load();
    }

    public void load() {
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

        for (String format : config.getSection("formats").getKeys(false)) {
            formats.put(format, config.getString("formats." + format));
        }

        parseFormats();
    }

    public void parseFormats() {
        for (String id : formats.keySet()) {
            String format = formats.get(id);
            String[] sections = format.split("[{}]");

            List<String> parsed = new ArrayList<>();

            for (String s : sections) {
                if (s.isEmpty()) continue;
                parsed.add(s);
            }

            parsedFormats.put(id, parsed);
        }
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
}
