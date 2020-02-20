package me.lilac.rosechat.placeholder;

import java.util.Map;

public class TextPlaceholder {

    private boolean useGroups;
    private String defaultText;
    private Map<String, String> groups;

    public boolean shouldUseGroups() {
        return useGroups;
    }

    public void setUseGroups(boolean useGroups) {
        this.useGroups = useGroups;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public Map<String, String> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }
}
