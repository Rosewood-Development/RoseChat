package dev.rosewood.rosechat.chat;

public class ChatReplacement {

    private String id;
    private String text;
    private String replacement;
    private String hoverText;

    public ChatReplacement(String id, String text, String replacement, String hoverText) {
        this.id = id;
        this.text = text;
        this.replacement = replacement;
        this.hoverText = hoverText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public String getHoverText() {
        return hoverText;
    }

    public void setHoverText(String hoverText) {
        this.hoverText = hoverText;
    }
}
