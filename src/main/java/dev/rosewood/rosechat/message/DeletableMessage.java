package dev.rosewood.rosechat.message;

import java.util.UUID;

public class DeletableMessage {

    private UUID uuid;
    private String json;
    private boolean isClient;

    public DeletableMessage(UUID uuid, String json, boolean isClient) {
        this.uuid = uuid;
        this.json = json;
        this.isClient = isClient;
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
}