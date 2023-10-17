package dev.rosewood.rosechat.placeholders;

import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;
import java.util.HashMap;
import java.util.Map;

public class CustomPlaceholder {

    private final String id;
    protected final Map<String, PlaceholderCondition> placeholders;

    public CustomPlaceholder(String id) {
        this.id = id;
        this.placeholders = new HashMap<>();
    }

    public Map<String, PlaceholderCondition> getPlaceholders() {
        return this.placeholders;
    }

    public void add(String id, PlaceholderCondition placeholder) {
        this.placeholders.put(id, placeholder);
    }

    public PlaceholderCondition get(String id) {
        return this.placeholders.get(id);
    }

    public String getId() {
        return this.id;
    }

}
