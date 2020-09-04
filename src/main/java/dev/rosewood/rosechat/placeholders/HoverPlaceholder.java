package dev.rosewood.rosechat.placeholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoverPlaceholder extends Placeholder {

    private Map<String, List<String>> groups;
    private Map<String, String> groupsAsStrings;

    public List<String> getHoverFromGroup(String group) {
        return groups.containsKey(group) ? groups.get(group) : groups.get("default");
    }

    public String getHoverStringFromGroup(String group) {
        return groupsAsStrings.containsKey(group) ? groupsAsStrings.get(group) : groupsAsStrings.get("default");
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    public Map<String, String> getGroupsAsStrings() {
        return groupsAsStrings;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;

        groupsAsStrings = new HashMap<>();
        for (String group : groups.keySet()) {
            List<String> list = groups.get(group);
            StringBuilder hoverSb = new StringBuilder();

            int i = 0;
            for (String str : list) {
                if (i != 0) hoverSb.append("\n").append(str);
                else hoverSb.append(str );
                i++;
            }

            groupsAsStrings.put(group, hoverSb.toString());
        }
    }
}
