package dev.rosewood.rosechat.placeholders;

import java.util.Map;

public class TextPlaceholder {

    private Map<String, String> groups;

    public String getTextFromGroup(String group) {
        return groups.get(group);
    }

    public Map<String, String> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }
}
