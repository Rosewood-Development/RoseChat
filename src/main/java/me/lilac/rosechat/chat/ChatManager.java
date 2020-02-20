package me.lilac.rosechat.chat;

import java.util.*;
import java.util.stream.Collectors;

public class ChatManager {

    private Map<UUID, UUID> lastReply;
    private Map<UUID, List<String>> messages;
    private List<String> colorCodes;
    private List<String> formattingCodes;

    public ChatManager() {
        lastReply = new HashMap<>();
        messages = new HashMap<>();
        colorCodes = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "a", "b", "c", "d", "e", "f");
        formattingCodes = Arrays.asList("k", "l", "m", "n", "o");
    }

    public String removeColours(String message) {
        for (String color : getColorCodes()) {
            if (message.contains("&" + color))
                message = message.replace("&" + color, "");
        }

        return message;
    }

    public String removeFormatting(String message) {
        for (String format : getFormattingCodes()) {
            if (message.toLowerCase().contains("&" + format.toLowerCase()))
                message = message.replace("&" + format, "");
        }

        return message;
    }

    public Map<UUID, List<String>> getMessages() {
        return messages;
    }

    public List<String> getMessagesReversed(UUID player) {
        if (!messages.containsKey(player)) return null;
        return messages.get(player).stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
    }

    public List<String> getColorCodes() {
        return colorCodes;
    }

    public List<String> getFormattingCodes() {
        return formattingCodes;
    }

    public Map<UUID, UUID> getLastReply() {
        return lastReply;
    }
}
