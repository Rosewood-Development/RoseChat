package dev.rosewood.rosechat.message;

public enum PermissionArea {

    // When sending a private message.
    MESSAGE,
    // When sending a message in a channel.
    CHANNEL,
    // When sending a message in a group chat.
    GROUP,
    // When setting a nickname.
    NICKNAME,
    // When setting a chat color.
    CHATCOLOR,
    // Removes all need for permissions.
    NONE

}
