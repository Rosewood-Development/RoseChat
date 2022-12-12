package dev.rosewood.rosechat.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrivateMessageInfo {

    private final RoseSender sender;
    private final RoseSender receiver;
    private final List<UUID> spies;

    public PrivateMessageInfo(RoseSender sender, RoseSender receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.spies = new ArrayList<>();
    }

    public RoseSender getSender() {
        return this.sender;
    }

    public RoseSender getReceiver() {
        return this.receiver;
    }

    public void addSpy(RoseSender spy) {
        this.spies.add(spy.getUUID());
    }

    public List<UUID> getSpies() {
        return this.spies;
    }

}
