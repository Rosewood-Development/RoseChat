package me.lilac.rosechat.placeholder;

public class CustomPlaceholder {

    private TextPlaceholder text;
    private HoverPlaceholder hover;
    private ClickPlaceholder click;

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
