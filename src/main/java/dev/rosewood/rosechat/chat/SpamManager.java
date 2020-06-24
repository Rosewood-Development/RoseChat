package dev.rosewood.rosechat.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpamManager {

    // compare messages sent, SPAM / spam / spam 1 / spam 2, see what can be done?

    private Map<UUID, List<String>> sentMessages;

    public SpamManager() {
        sentMessages = new HashMap<>();
    }

    public String getLastMessage(UUID uuid) {
        return sentMessages.get(uuid).get(sentMessages.get(uuid).size() - 1);
    }

    public boolean isSpam(UUID uuid, String message) {
        return false;
    }
}
