package dev.rosewood.rosechat.placeholders;

import java.util.Map;

public class TextPlaceholder extends Placeholder {

    private Map<String, String> groups;

    public String getTextFromGroup(String group) {
        return this.groups.containsKey(group) ? this.groups.get(group) : this.groups.get("default");
    }

    public Map<String, String> getGroups() {
        return this.groups;
    }

    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }
}
