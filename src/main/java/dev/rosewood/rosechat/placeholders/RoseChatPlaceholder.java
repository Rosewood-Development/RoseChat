package dev.rosewood.rosechat.placeholders;

import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;

public class RoseChatPlaceholder {

    private String id;
    private PlaceholderCondition text;
    private PlaceholderCondition hover;
    private PlaceholderCondition click;

    public RoseChatPlaceholder(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PlaceholderCondition getText() {
        return this.text;
    }

    public void setText(PlaceholderCondition text) {
        this.text = text;
    }

    public PlaceholderCondition getHover() {
        return this.hover;
    }

    public void setHover(PlaceholderCondition hover) {
        this.hover = hover;
    }

    public PlaceholderCondition getClick() {
        return this.click;
    }

    public void setClick(PlaceholderCondition click) {
        this.click = click;
    }

}
