package dev.rosewood.rosechat.chat;

public class ChatReplacement {

    private final String id;
    private String text;
    private String replacement;
    private String hoverText;
    private String font;

    /**
     * Creates a new chat replacement with the given ID.
     * @param id The ID to use.
     * @param text The text to be replaced.
     * @param replacement The replacement to be used.
     * @param hoverText The text that appears when the replacement is hovered.
     * @param font The font to be used.
     */
    public ChatReplacement(String id, String text, String replacement, String hoverText, String font) {
        this.id = id;
        this.text = text;
        this.replacement = replacement;
        this.hoverText = hoverText;
        this.font = font;
    }

    /**
     * Creates a new chat replacement with the given ID.
     * @param id The ID to use.
     * @param text The text to be replaced.
     * @param replacement The replacement to be used.
     * @param hoverText The text that appears when the replacement is hovered.
     */
    public ChatReplacement(String id, String text, String replacement, String hoverText) {
        this(id, text, replacement, hoverText, "default");
    }

    /**
     * Gets the ID.
     * @return The ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the text to be replaced.
     * @return The text to replace.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text.
     * @param text The text to use.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the replacement.
     * @return The replacement.
     */
    public String getReplacement() {
        return this.replacement;
    }

    /**
     * Sets the replacement
     * @param replacement The replacement to use.
     */
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * Gets the text shown when the replacement is hovered.
     * @return The hover text.
     */
    public String getHoverText() {
        return this.hoverText;
    }

    /**
     * Sets the text to be shown when the replacement is hovered.
     * @param hoverText The text to be used.
     */
    public void setHoverText(String hoverText) {
        this.hoverText = hoverText;
    }

    /**
     * Gets the font.
     * @return The font.
     */
    public String getFont() {
        return this.font;
    }

    /**
     * Sets the font.
     * @param font The font to be used.
     */
    public void setFont(String font) {
        this.font = font;
    }
}
