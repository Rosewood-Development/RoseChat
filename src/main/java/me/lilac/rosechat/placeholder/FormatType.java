package me.lilac.rosechat.placeholder;

import me.lilac.rosechat.storage.Settings;

public enum FormatType {

    CHAT(Settings.getChatFormat()),
    MSG_SENT(Settings.getMessageSentFormat()),
    MSG_RECEIVE(Settings.getMessageReceivedFormat()),
    STAFF_CHAT(Settings.getStaffChatFormat());

    private String format;

    FormatType(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
