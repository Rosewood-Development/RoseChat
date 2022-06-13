package dev.rosewood.rosechat.placeholders;

import java.util.HashMap;
import java.util.Map;

public class DiscordPlaceholder {

    private final String id;
    private final Map<String, CustomPlaceholder> placeholders;

    public DiscordPlaceholder(String id) {
        this.id = id;
        this.placeholders = new HashMap<>();
    }

    public Map<String, CustomPlaceholder> getPlaceholders() {
        return this.placeholders;
    }

    public void addPlaceholder(String id, CustomPlaceholder placeholder) {
        this.placeholders.put(id, placeholder);
    }

    public CustomPlaceholder getPlaceholder(String id) {
        return this.placeholders.get(id);
    }

}
