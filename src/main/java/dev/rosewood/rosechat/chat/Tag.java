package dev.rosewood.rosechat.chat;

import org.bukkit.Sound;

public class Tag {

    private final String id;
    private String prefix;
    private String suffix;
    private String replacement;
    private boolean tagOnlinePlayers;
    private boolean matchLength;
    private Sound sound;
    private String format;

    /**
     * Creates a new tag with the given ID.
     * @param id The ID to use.
     */
    public Tag(String id) {
        this.id = id;
    }

    /**
     * Gets the ID of the tag.
     * @return The ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the prefix of the tag.
     * @return The prefix.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Sets the prefix of the tag.
     * @param prefix The prefix to use.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the suffix of the tag.
     * @return The suffix.
     */
    public String getSuffix() {
        return this.suffix;
    }

    /**
     * Sets the suffix of the tag.
     * @param suffix The suffix to use.
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Whether or not this tag should tag online players?
     * @return True if the tag should tag online players.
     */
    public boolean shouldTagOnlinePlayers() {
        return this.tagOnlinePlayers;
    }

    /**
     * Sets whether this tag should tag online players.
     * @param tagOnlinePlayers Whether or not this tag should tag online players.
     */
    public void setTagOnlinePlayers(boolean tagOnlinePlayers) {
        this.tagOnlinePlayers = tagOnlinePlayers;
    }

    /**
     * Whether or not the tag should match the length of the text.
     * @return True if the tag should match the length of the text.
     */
    public boolean shouldMatchLength() {
        return this.matchLength;
    }

    /**
     * Sets whether the tag should match the length of the text.
     * @param matchLength Whether or not this tag should match the length of the text.
     */
    public void setMatchLength(boolean matchLength) {
        this.matchLength = matchLength;
    }

    /**
     * Gets the sound that the tag should play.
     * @return The sound to play.
     */
    public Sound getSound() {
        return this.sound;
    }

    /**
     * Sets the sound that the tag should play.
     * @param sound The sound to play.
     */
    public void setSound(Sound sound) {
        this.sound = sound;
    }

    /**
     * Gets the format to use for the tag.
     * @return The format to use.
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * Sets the format to use for the tag.
     * @param format The format to use.
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
