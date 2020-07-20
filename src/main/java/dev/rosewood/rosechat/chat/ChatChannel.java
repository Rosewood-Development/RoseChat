package dev.rosewood.rosechat.chat;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatChannel {

    private String id;
    private boolean defaultChannel;
    private String format;
    private String formatId;
    private int radius = -1;
    private String world;
    private boolean autoJoin;
    private boolean checkCaps;
    private boolean checkUrl;
    private boolean checkSpam;
    private boolean checkLanguage;
    private boolean chatPlaceholders;
    private List<String> disabledTags;
    private List<String> disabledEmotes;

    private List<UUID> players;

    public ChatChannel(String id, String format, boolean defaultChannel) {
        this(id, format);
        this.defaultChannel = defaultChannel;
    }

    public ChatChannel(String id, String format) {
        this.id = id;
        this.format = format;
        this.formatId = "channel-" + id;
        this.players = new ArrayList<>();
    }

    public void message(Player sender, String message) {
        // message wrapper stuff here?
    }

    public void add(Player player) {
        add(player.getUniqueId());
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    public boolean contains(Player player) {
        return contains(player.getUniqueId());
    }

    public void add(UUID uuid) {
        players.add(uuid);
    }

    public void remove(UUID uuid) {
        players.remove(uuid);
    }

    public boolean contains(UUID uuid) {
        return players.contains(uuid);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatId() {
        return formatId;
    }

    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public boolean isCheckCaps() {
        return checkCaps;
    }

    public void setCheckCaps(boolean checkCaps) {
        this.checkCaps = checkCaps;
    }

    public boolean isCheckLanguage() {
        return checkLanguage;
    }

    public void setCheckLanguage(boolean checkLanguage) {
        this.checkLanguage = checkLanguage;
    }

    public boolean isCheckSpam() {
        return checkSpam;
    }

    public void setCheckSpam(boolean checkSpam) {
        this.checkSpam = checkSpam;
    }

    public boolean isCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(boolean checkUrl) {
        this.checkUrl = checkUrl;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void setPlayers(List<UUID> players) {
        this.players = players;
    }

    public boolean hasChatPlaceholders() {
        return chatPlaceholders;
    }

    public void setChatPlaceholders(boolean chatPlaceholders) {
        this.chatPlaceholders = chatPlaceholders;
    }

    public List<String> getDisabledEmotes() {
        return disabledEmotes;
    }

    public void setDisabledEmotes(List<String> disabledEmotes) {
        this.disabledEmotes = disabledEmotes;
    }

    public List<String> getDisabledTags() {
        return disabledTags;
    }

    public void setDisabledTags(List<String> disabledTags) {
        this.disabledTags = disabledTags;
    }
}
