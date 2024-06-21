package dev.rosewood.rosechat.message;

import java.util.UUID;

public class DeletableMessage {

    private final UUID uuid;
    private UUID sender;
    private String original;
    private String json;
    private String discordId;
    private String channel;
    private boolean isClient;

    public DeletableMessage(UUID uuid) {
        this.uuid = uuid;
    }

    public DeletableMessage(UUID uuid, String json, boolean isClient) {
        this(uuid);

        this.json = json;
        this.isClient = isClient;
    }

    public DeletableMessage(UUID uuid, String json, boolean isClient, String discordId) {
        this(uuid, json, isClient);

        this.discordId = discordId;
    }

    public DeletableMessage(UUID uuid, String json, boolean isClient, String discordId, String channel) {
        this(uuid, json, isClient);

        this.discordId = discordId;
        this.channel = channel;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean isClient() {
        return this.isClient;
    }

    public void setClient(boolean client) {
        this.isClient = client;
    }

    public String getDiscordId() {
        return this.discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public UUID getSender() {
        return this.sender;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getOriginal() {
        return this.original;
    }

}
