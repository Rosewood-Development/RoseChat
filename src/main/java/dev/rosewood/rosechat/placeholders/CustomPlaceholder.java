package dev.rosewood.rosechat.placeholders;

public class CustomPlaceholder {

    private String id;
    private ConditionalPlaceholder text;
    private ConditionalPlaceholder hover;
    private ConditionalPlaceholder click;

    public CustomPlaceholder(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConditionalPlaceholder getText() {
        return this.text;
    }

    public void setText(ConditionalPlaceholder text) {
        this.text = text;
    }

    public ConditionalPlaceholder getHover() {
        return this.hover;
    }

    public void setHover(ConditionalPlaceholder hover) {
        this.hover = hover;
    }

    public ConditionalPlaceholder getClick() {
        return this.click;
    }

    public void setClick(ConditionalPlaceholder click) {
        this.click = click;
    }
}
