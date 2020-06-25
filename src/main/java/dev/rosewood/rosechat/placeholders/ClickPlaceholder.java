package dev.rosewood.rosechat.placeholders;

import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;

public class ClickPlaceholder {

    private Map<String, ClickEvent> groups;

    public ClickEvent getClickFromGroup(String group) {
        return groups.containsKey(group) ? groups.get(group) : groups.get("default");
    }

    public ClickEvent.Action getActionFromGroup(String group) {
        return groups.containsKey(group) ? groups.get(group).getAction() : groups.get("default").getAction();
    }

    public String getValueFromGroup(String group) {
        return groups.containsKey(group) ? groups.get(group).getValue() : groups.get("default").getValue();
    }

    public Map<String, ClickEvent> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, ClickEvent> groups) {
        this.groups = groups;
    }
}
