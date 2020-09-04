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
    private List<String> disabledReplacements;
    private List<String> servers;

    private List<UUID> players;

    public ChatChannel(String id, String format, boolean defaultChannel) {
        this(id, format);
        this.defaultChannel = defaultChannel;
        this.players = new ArrayList<>();
        this.disabledTags = new ArrayList<>();
        this.disabledReplacements = new ArrayList<>();
        this.servers = new ArrayList<>();
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

    public ChatChannel setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isDefaultChannel() {
        return defaultChannel;
    }

    public ChatChannel setDefaultChannel(boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public ChatChannel setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormatId() {
        return formatId;
    }

    public ChatChannel setFormatId(String formatId) {
        this.formatId = formatId;
        return this;
    }

    public int getRadius() {
        return radius;
    }

    public ChatChannel setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public String getWorld() {
        return world;
    }

    public ChatChannel setWorld(String world) {
        this.world = world;
        return this;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public ChatChannel setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
        return this;
    }

    public boolean isCheckCaps() {
        return checkCaps;
    }

    public ChatChannel setCheckCaps(boolean checkCaps) {
        this.checkCaps = checkCaps;
        return this;
    }

    public boolean isCheckLanguage() {
        return checkLanguage;
    }

    public ChatChannel setCheckLanguage(boolean checkLanguage) {
        this.checkLanguage = checkLanguage;
        return this;
    }

    public boolean isCheckSpam() {
        return checkSpam;
    }

    public ChatChannel setCheckSpam(boolean checkSpam) {
        this.checkSpam = checkSpam;
        return this;
    }

    public boolean isCheckUrl() {
        return checkUrl;
    }

    public ChatChannel setCheckUrl(boolean checkUrl) {
        this.checkUrl = checkUrl;
        return this;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public ChatChannel setPlayers(List<UUID> players) {
        this.players = players;
        return this;
    }

    public boolean hasChatPlaceholders() {
        return chatPlaceholders;
    }

    public ChatChannel setChatPlaceholders(boolean chatPlaceholders) {
        this.chatPlaceholders = chatPlaceholders;
        return this;
    }

    public List<String> getDisabledEmotes() {
        return disabledReplacements;
    }

    public ChatChannel setDisabledReplacements(List<String> disabledReplacements) {
        this.disabledReplacements = disabledReplacements;
        return this;
    }

    public List<String> getDisabledTags() {
        return disabledTags;
    }

    public ChatChannel setDisabledTags(List<String> disabledTags) {
        this.disabledTags = disabledTags;
        return this;
    }

    public List<String> getServers() {
        return servers;
    }

    public ChatChannel setServers(List<String> servers) {
        this.servers = servers;
        return this;
    }
}
