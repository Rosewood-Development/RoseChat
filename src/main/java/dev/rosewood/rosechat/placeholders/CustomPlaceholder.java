package dev.rosewood.rosechat.placeholders;

public class CustomPlaceholder {

    private String id;
    private TextPlaceholder text;
    private HoverPlaceholder hover;
    private ClickPlaceholder click;

    public CustomPlaceholder(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TextPlaceholder getText() {
        return this.text;
    }

    public void setText(TextPlaceholder text) {
        this.text = text;
    }

    public HoverPlaceholder getHover() {
        return this.hover;
    }

    public void setHover(HoverPlaceholder hover) {
        this.hover = hover;
    }

    public ClickPlaceholder getClick() {
        return this.click;
    }

    public void setClick(ClickPlaceholder click) {
        this.click = click;
    }
}
