package dev.rosewood.rosechat.chat;

import org.bukkit.Sound;

public class Tag {

    private String id;
    private String prefix;
    private boolean tagOnlinePlayers;
    private Sound sound;
    private String format;

    public Tag(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Tag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public Tag setTagOnlinePlayers(boolean tagOnlinePlayers) {
        this.tagOnlinePlayers = tagOnlinePlayers;
        return this;
    }

    public boolean shouldTagOnlinePlayers() {
        return tagOnlinePlayers;
    }

    public Tag setSound(Sound sound) {
        this.sound = sound;
        return this;
    }

    public Sound getSound() {
        return sound;
    }

    public Tag setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }
}
