package me.lilac.rosechat.placeholder;

import java.util.List;
import java.util.Map;

public class HoverPlaceholder {

    private boolean useGroups;
    private String defaultHoverEvent;
    private Map<String, String> groups;

    public boolean shouldUseGroups() {
        return useGroups;
    }

    public void setUseGroups(boolean useGroups) {
        this.useGroups = useGroups;
    }

    public String getDefaultHoverEvent() {
        return defaultHoverEvent;
    }

    public void setDefaultHoverEvent(String defaultHoverEvent) {
        this.defaultHoverEvent = defaultHoverEvent;
    }

    public Map<String, String> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }
}
