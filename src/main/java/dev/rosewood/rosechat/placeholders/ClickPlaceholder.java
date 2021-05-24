package dev.rosewood.rosechat.placeholders;

import net.md_5.bungee.api.chat.ClickEvent;
import java.util.Map;

public class ClickPlaceholder extends Placeholder {

    private Map<String, ClickEvent> groups;

    public ClickEvent getClickFromGroup(String group) {
        return this.groups.containsKey(group) ? this.groups.get(group) : this.groups.get("default");
    }

    public ClickEvent.Action getActionFromGroup(String group) {
        return this.groups.containsKey(group) ? this.groups.get(group).getAction() : this.groups.get("default").getAction();
    }

    public String getValueFromGroup(String group) {
        return this.groups.containsKey(group) ? this.groups.get(group).getValue() : this.groups.get("default").getValue();
    }

    public Map<String, ClickEvent> getGroups() {
        return this.groups;
    }

    public void setGroups(Map<String, ClickEvent> groups) {
        this.groups = groups;
    }
}
