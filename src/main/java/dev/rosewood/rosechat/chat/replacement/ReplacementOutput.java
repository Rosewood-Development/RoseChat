package dev.rosewood.rosechat.chat.replacement;

import org.bukkit.Sound;

public class ReplacementOutput {

    private String text;
    private String hover;
    private String font;
    private String discordOutput;
    private Sound sound;
    private boolean tagOnlinePlayers;
    private boolean matchLength;
    private boolean colorRetention;

    public ReplacementOutput() {

    }

    public ReplacementOutput(ReplacementOutput output) {
        this.text = output.text;
        this.hover = output.hover;
        this.font = output.font;
        this.discordOutput = output.discordOutput;
        this.sound = output.sound;
        this.tagOnlinePlayers = output.tagOnlinePlayers;
        this.matchLength = output.matchLength;
        this.colorRetention = output.colorRetention;
    }

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

    public String getDiscordOutput() {
        return this.discordOutput;
    }

    public void setDiscordOutput(String discordOutput) {
        this.discordOutput = discordOutput;
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
