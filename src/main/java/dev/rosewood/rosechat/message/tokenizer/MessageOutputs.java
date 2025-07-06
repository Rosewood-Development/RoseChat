package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.RosePlayer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MessageOutputs {

    private final List<UUID> taggedPlayers;
    private final Set<String> missingPermissions;
    private final List<String> serverCommands;
    private final List<String> playerCommands;
    private String sound;
    private String message;
    private RosePlayer placeholderTarget;

    public MessageOutputs() {
        this.taggedPlayers = new ArrayList<>();
        this.missingPermissions = new HashSet<>();
        this.serverCommands = new ArrayList<>();
        this.playerCommands = new ArrayList<>();
    }

    public List<UUID> getTaggedPlayers() {
        return this.taggedPlayers;
    }

    public List<String> getServerCommands() {
        return this.serverCommands;
    }

    public List<String> getPlayerCommands() {
        return this.playerCommands;
    }

    public String getSound() {
        return this.sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<String> getMissingPermissions() {
        return this.missingPermissions;
    }

    public RosePlayer getPlaceholderTarget() {
        return this.placeholderTarget;
    }

    public void setPlaceholderTarget(RosePlayer placeholderTarget) {
        this.placeholderTarget = placeholderTarget;
    }

    public void reset() {
        this.taggedPlayers.clear();
        this.missingPermissions.clear();
        this.serverCommands.clear();
        this.playerCommands.clear();
        this.sound = null;
        this.message = null;
        this.placeholderTarget = null;
    }

}
