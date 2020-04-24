package dev.rosewood.rosechat.placeholders;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import net.md_5.bungee.api.chat.ClickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderManager {

    private YMLFile config;
    private Map<String, CustomPlaceholder> placeholders;

    public PlaceholderManager() {
        config = RoseChat.getInstance().getConfigFile();
        placeholders = new HashMap<>();
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
            for (String hoverGroup : config.getSection("custom-placeholders." + id + ".hover").getKeys(false)) {
                hoverLines = config.getStringList("custom-placeholders." + id + ".hover." + hoverGroup);
                hoverPlaceholders.put(hoverGroup, hoverLines);
            }

            Map<String, RoseClickEvent> clickPlaceholders = new HashMap<>();
            RoseClickEvent clickEvent;
            for (String clickGroup : config.getSection("custom-placeholders." + id + ".click").getKeys(false)) {
                String clickEventStr = config.getString("custom-placeholders." + id + ".click." + clickGroup + ".action");
                String extra = config.getString("custom-placeholders." + id + ".click." + clickGroup + ".extra");

                clickEvent = new RoseClickEvent(ClickEvent.Action.valueOf(clickEventStr), extra);
                clickPlaceholders.put(clickGroup, clickEvent);
            }

            textPlaceholder.setGroups(textPlaceholders);
            hoverPlaceholder.setGroups(hoverPlaceholders);
            clickPlaceholder.setGroups(clickPlaceholders);
            placeholder.setText(textPlaceholder);
            if (hoverPlaceholder != null) placeholder.setHover(hoverPlaceholder);
            if (clickPlaceholder != null) placeholder.setClick(clickPlaceholder);
        }
    }

    public CustomPlaceholder getPlaceholder(String id) {
        return placeholders.get(id);
    }

    public Map<String, CustomPlaceholder> getPlaceholders() {
        return placeholders;
    }
}
