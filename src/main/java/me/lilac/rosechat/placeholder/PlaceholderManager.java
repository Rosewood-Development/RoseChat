package me.lilac.rosechat.placeholder;

import me.lilac.rosechat.Rosechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderManager {

    private Rosechat plugin;
    private Map<String, CustomPlaceholder> placeholders;
    private Map<FormatType, List<String>> orphanedPlaceholders;

    public PlaceholderManager() {
        plugin = Rosechat.getInstance();
        placeholders = new HashMap<>();
        orphanedPlaceholders = new HashMap<>();
    }

    public Map<String, CustomPlaceholder> getPlaceholders() {
        return placeholders;
    }

    /*
    They're without PlaceholderAPI applied.
    They're... PAPI-less.
     */
    public Map<FormatType, List<String>> getOrphanedPlaceholders() {
        return orphanedPlaceholders;
    }

    public void init() {
        createOrphans(FormatType.CHAT);
        createOrphans(FormatType.MSG_SENT);
        createOrphans(FormatType.MSG_RECEIVE);
        createOrphans(FormatType.STAFF_CHAT);
    }

    private void createOrphans(FormatType format) {
        String[] sections = format.getFormat().split("\\}\\{");
        List<String> placeholders = new ArrayList<>();

        for (String section : sections) {
            if (section.contains("{")) section = section.replace("{", "");
            if (section.contains("}")) section = section.replace("}", "");
            placeholders.add(section);
        }

        orphanedPlaceholders.put(format, placeholders);
    }
}
