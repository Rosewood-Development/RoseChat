package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.message.RosePlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrivateMessageInfo {

    private final RosePlayer sender;
    private final RosePlayer receiver;
    private final List<UUID> spies;

    /**
     * Creates a new PrivateMessageInfo, to store information about a private message.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param receiver The {@link RosePlayer} who is receiving the message.
     */
    public PrivateMessageInfo(RosePlayer sender, RosePlayer receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.spies = new ArrayList<>();
    }

    /**
     * @return The {@link RosePlayer} who is sending the message.
     */
    public RosePlayer getSender() {
        return this.sender;
    }

    /**
     * @return The {@link RosePlayer} who is receiving the message.
     */
    public RosePlayer getReceiver() {
        return this.receiver;
    }

    /**
     * Adds a spy to the message.
     * @param spy The {@link RosePlayer} who is spying on the message.
     */
    public void addSpy(RosePlayer spy) {
        this.spies.add(spy.getUUID());
    }

    /**
     * @return A list of {@link UUID}s of player's who are spying on this message.
     */
    public List<UUID> getSpies() {
        return this.spies;
    }

}
