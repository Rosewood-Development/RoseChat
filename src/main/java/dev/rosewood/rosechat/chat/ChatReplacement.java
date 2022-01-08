package dev.rosewood.rosechat.chat;

public class ChatReplacement {

    private final String id;
    private String text;
    private String replacement;
    private String hoverText;
    private String font;
    private boolean regex;

    /**
     * Creates a new chat replacement with the given ID.
     * @param id The ID to use.
     * @param text The text to be replaced.
     * @param replacement The replacement to be used.
     * @param hoverText The text that appears when the replacement is hovered.
     * @param font The font to be used.
     */
    public ChatReplacement(String id, String text, String replacement, String hoverText, String font, boolean regex) {
        this.id = id;
        this.text = text;
        this.replacement = replacement;
        this.hoverText = hoverText;
        this.font = font;
        this.regex = regex;
    }

    /**
     * Creates a new chat replacement with the given ID.
     * @param id The ID to use.
     * @param text The text to be replaced.
     * @param replacement The replacement to be used.
     */
    public ChatReplacement(String id, String text, String replacement, boolean regex) {
        this(id, text, replacement, null, "default", regex);
    }

    /**
     * @return The ID of the replacement.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return The text to replace.
     */
    public String getText() {
        return this.text;
    }

    /**
     * @param text The text to use.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return The replacement.
     */
    public String getReplacement() {
        return this.replacement;
    }

    /**
     * @param replacement The replacement to use.
     */
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * @return The text shown when the replacement is hovered.
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
     * @return The font.
     */
    public String getFont() {
        return this.font;
    }

    /**
     * @param font The font to be used.
     */
    public void setFont(String font) {
        this.font = font;
    }

    /**
     * @return True if the replacement uses regex.
     */
    public boolean isRegex() {
        return this.regex;
    }

    /**
     * @param regex Whether the replacement is using regex.
     */
    public void setRegex(boolean regex) {
        this.regex = regex;
    }
}
