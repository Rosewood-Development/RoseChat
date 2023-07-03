package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.chat.FilterType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Sound;

public class MessageOutputs {

    private final List<UUID> taggedPlayers;
    private Sound tagSound;
    private FilterType filterType;
    private boolean isBlocked;

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

    public FilterType getFilterType() {
        return this.filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }

    public void setBlocked() {
        this.isBlocked = true;
    }

    public MessageOutputs merge(MessageOutputs newOutputs) {
        this.taggedPlayers.addAll(newOutputs.getTaggedPlayers());
        if (newOutputs.getTagSound() != null)
            this.tagSound = newOutputs.getTagSound();
        if (newOutputs.getFilterType() != null)
            this.filterType = newOutputs.getFilterType();
        if (newOutputs.isBlocked())
            this.isBlocked = true;
        return this;
    }

}
