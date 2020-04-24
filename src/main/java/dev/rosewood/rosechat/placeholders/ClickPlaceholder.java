package dev.rosewood.rosechat.placeholders;

import java.util.Map;

public class ClickPlaceholder {

    private Map<String, RoseClickEvent> groups;

    private RoseClickEvent getClickFromGroup(String group) {
        return groups.get(group);
    }

    public Map<String, RoseClickEvent> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, RoseClickEvent> groups) {
        this.groups = groups;
    }
}
