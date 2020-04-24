package dev.rosewood.rosechat.channels;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import org.bukkit.entity.Player;

public class ChatManager {

    private YMLFile data;
    private Player player;
    private String message;

    public ChatManager(Player player, String message) {
        data = RoseChat.getInstance().getDataFile();
        this.player = player;
        this.message = message;
        parseMessage();
    }

    public String getMessage() {
        return message;
    }

    public void parseMessage() {
        parseCaps();
    }

    public void parseCaps() {
        if (!data.getBoolean("caps-check") || player.hasPermission("rosechat.bypass.caps")) return;

        int maxCaps = data.getInt("max-amount-of-caps");
        int uppercaseCount = 0;

        for (int i = 0; i < message.length(); i++) {
            if (Character.isUpperCase(message.charAt(i))) uppercaseCount++;
        }

        if (uppercaseCount < maxCaps) return;

        if (data.getBoolean("lowercase-caps")) message = message.toLowerCase();
        else message = null;
    }
}
