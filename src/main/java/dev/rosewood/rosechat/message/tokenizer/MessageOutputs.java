package dev.rosewood.rosechat.message.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Sound;

public class MessageOutputs {

    private final List<UUID> taggedPlayers;
    private Sound tagSound;

    public MessageOutputs() {
        this.taggedPlayers = new ArrayList<>();
    }

    public List<UUID> getTaggedPlayers() {
        return this.taggedPlayers;
    }

    public Sound getTagSound() {
        return this.tagSound;
    }

    public void setTagSound(Sound tagSound) {
        this.tagSound = tagSound;
    }

    public MessageOutputs merge(MessageOutputs newOutputs) {
        this.taggedPlayers.addAll(newOutputs.getTaggedPlayers());
        if (newOutputs.getTagSound() != null)
            this.tagSound = newOutputs.getTagSound();
        return this;
    }

}
