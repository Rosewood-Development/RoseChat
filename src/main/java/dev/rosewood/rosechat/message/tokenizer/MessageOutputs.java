package dev.rosewood.rosechat.message.tokenizer;

import org.bukkit.Sound;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MessageOutputs {

    private final List<UUID> taggedPlayers;
    private String tagSound;
    private final Set<String> missingPermissions;

    public MessageOutputs() {
        this.taggedPlayers = new ArrayList<>();
        this.missingPermissions = new HashSet<>();
    }

    public List<UUID> getTaggedPlayers() {
        return this.taggedPlayers;
    }

    public String getTagSound() {
        return this.tagSound;
    }

    public void setTagSound(String tagSound) {
        this.tagSound = tagSound;
    }

    public Set<String> getMissingPermissions() {
        return this.missingPermissions;
    }

    public void merge(MessageOutputs newOutputs) {
        this.taggedPlayers.addAll(newOutputs.getTaggedPlayers());
        if (newOutputs.getTagSound() != null)
            this.tagSound = newOutputs.getTagSound();

        this.missingPermissions.addAll(newOutputs.getMissingPermissions());
    }

}
