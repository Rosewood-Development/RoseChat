package dev.rosewood.rosechat.chat;

import org.bukkit.Sound;

public class Tag {

    private final String id;
    private String prefix;
    private String suffix;
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
     * @return The ID of the tag.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return The prefix of the tag.
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
     * @return The suffix of the tag.
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
     * @return True if the tag should tag online players.
     */
    public boolean shouldTagOnlinePlayers() {
        return this.tagOnlinePlayers;
    }

    /**
     * @param tagOnlinePlayers Whether this tag should tag online players.
     */
    public void setTagOnlinePlayers(boolean tagOnlinePlayers) {
        this.tagOnlinePlayers = tagOnlinePlayers;
    }

    /**
     * @return True if the tag should match the length of the text.
     */
    public boolean shouldMatchLength() {
        return this.matchLength;
    }

    /**
     * @param matchLength Whether this tag should match the length of the text.
     */
    public void setMatchLength(boolean matchLength) {
        this.matchLength = matchLength;
    }

    /**
     * @return The sound to play when tagged.
     */
    public Sound getSound() {
        return this.sound;
    }

    /**
     * @param sound The sound to play when tagged.
     */
    public void setSound(Sound sound) {
        this.sound = sound;
    }

    /**
     * @return The format to use.
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * @param format The format to use.
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
