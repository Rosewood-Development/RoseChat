package dev.rosewood.rosechat.placeholders;

import java.util.List;
import java.util.Map;

public class HoverPlaceholder {

    private Map<String, List<String>> groups;

    public List<String> getHoverFromGroup(String group) {
        return groups.get(group);
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;
    }
}
