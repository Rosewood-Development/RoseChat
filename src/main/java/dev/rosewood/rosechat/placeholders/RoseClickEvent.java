package dev.rosewood.rosechat.placeholders;

import net.md_5.bungee.api.chat.ClickEvent;

public class RoseClickEvent {

    private ClickEvent.Action action;
    private String extra;

    public RoseClickEvent(ClickEvent.Action action, String extra) {
        this.action = action;
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
