package me.lilac.rosechat.storage;

import org.bukkit.Sound;

import java.util.Map;

public class Settings {

    private static boolean capsCheck;
    private static boolean lowercaseCaps;
    private static boolean spamCheck;
    private static Map<String, String> blockedMessages;
    private static Sound messageSound;
    private static Sound broadcastSound;
    private static String chatFormat;
    private static String messageSentFormat;
    private static String messageReceivedFormat;
    private static String staffChatFormat;

    public static boolean shouldDoCapsCheck() {
        return capsCheck;
    }

    public static void setCapsCheck(boolean capsCheck) {
        Settings.capsCheck = capsCheck;
    }

    public static boolean shouldLowercaseCaps() {
        return lowercaseCaps;
    }

    public static void setLowercaseCaps(boolean lowercaseCaps) {
        Settings.lowercaseCaps = lowercaseCaps;
    }

    public static boolean shouldDoSpamCheck() {
        return spamCheck;
    }

    public static void setSpamCheck(boolean spamCheck) {
        Settings.spamCheck = spamCheck;
    }

    public static Map<String, String> getBlockedMessages() {
        return blockedMessages;
    }

    public static void setBlockedMessages(Map<String, String> blockedMessages) {
        Settings.blockedMessages = blockedMessages;
    }

    public static Sound getMessageSound() {
        return messageSound;
    }

    public static void setMessageSound(Sound messageSound) {
        Settings.messageSound = messageSound;
    }

    public static Sound getBroadcastSound() {
        return broadcastSound;
    }

    public static void setBroadcastSound(Sound broadcastSound) {
        Settings.broadcastSound = broadcastSound;
    }

    public static String getChatFormat() {
        return chatFormat;
    }

    public static void setChatFormat(String chatFormat) {
        Settings.chatFormat = chatFormat;
    }

    public static String getMessageSentFormat() {
        return messageSentFormat;
    }

    public static void setMessageSentFormat(String messageSentFormat) {
        Settings.messageSentFormat = messageSentFormat;
    }

    public static String getMessageReceivedFormat() {
        return messageReceivedFormat;
    }

    public static void setMessageReceivedFormat(String messageReceivedFormat) {
        Settings.messageReceivedFormat = messageReceivedFormat;
    }

    public static String getStaffChatFormat() {
        return staffChatFormat;
    }

    public static void setStaffChatFormat(String staffChatFormat) {
        Settings.staffChatFormat = staffChatFormat;
    }
}
