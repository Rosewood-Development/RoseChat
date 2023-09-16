package dev.rosewood.rosechat.chat.replacement;

import org.bukkit.Sound;

public class ReplacementOutput {

    private String text;
    private String hover;
    private String font;
    private Sound sound;
    private boolean tagOnlinePlayers;
    private boolean matchLength;
    private boolean colorRetention;

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHover() {
        return this.hover;
    }

    public void setHover(String hover) {
        this.hover = hover;
    }

    public String getFont() {
        return this.font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Sound getSound() {
        return this.sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public boolean shouldTagOnlinePlayers() {
        return this.tagOnlinePlayers;
    }

    public void setShouldTagOnlinePlayers(boolean tagOnlinePlayers) {
        this.tagOnlinePlayers = tagOnlinePlayers;
    }

    public boolean shouldMatchLength() {
        return this.matchLength;
    }

    public void setShouldMatchLength(boolean matchLength) {
        this.matchLength = matchLength;
    }

    public boolean hasColorRetention() {
        return this.colorRetention;
    }

    public void setHasColorRetention(boolean colorRetention) {
        this.colorRetention = colorRetention;
    }

}
