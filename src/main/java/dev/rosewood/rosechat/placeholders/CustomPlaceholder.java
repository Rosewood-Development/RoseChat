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
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TextPlaceholder getText() {
        return text;
    }

    public void setText(TextPlaceholder text) {
        this.text = text;
    }

    public HoverPlaceholder getHover() {
        return hover;
    }

    public void setHover(HoverPlaceholder hover) {
        this.hover = hover;
    }

    public ClickPlaceholder getClick() {
        return click;
    }

    public void setClick(ClickPlaceholder click) {
        this.click = click;
    }
}
