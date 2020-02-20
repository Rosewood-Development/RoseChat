package me.lilac.rosechat.placeholder;
import net.md_5.bungee.api.chat.ClickEvent;

import java.util.Map;

public class ClickPlaceholder {

    private boolean useGroups;
    private RoseClickEvent defaultClick;
    private Map<String, RoseClickEvent> groups;

    public boolean shouldUseGroups() {
        return useGroups;
    }

    public void setUseGroups(boolean useGroups) {
        this.useGroups = useGroups;
    }

    public RoseClickEvent getDefaultClick() {
        return defaultClick;
    }

    public void setDefaultClick(RoseClickEvent defaultClick) {
        this.defaultClick = defaultClick;
    }

    public Map<String, RoseClickEvent> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, RoseClickEvent> groups) {
        this.groups = groups;
    }

    public static class RoseClickEvent {

        private ClickEvent.Action action;
        private String extra;

        public RoseClickEvent(String action, String extra) {
            this.action = ClickEvent.Action.valueOf(action);
            this.extra = extra;
        }

        public ClickEvent.Action getAction() {
            return action;
        }

        public void setAction(ClickEvent.Action action) {
            this.action = action;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }
}
