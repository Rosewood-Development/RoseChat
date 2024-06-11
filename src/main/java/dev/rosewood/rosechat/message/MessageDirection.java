package dev.rosewood.rosechat.message;

public enum MessageDirection {

    // Sent from the player (either directly or indirectly), to the server.
    PLAYER_TO_SERVER,
    // Going from one server to another.
    SERVER_TO_SERVER,
    // The raw message going from server to another.
    SERVER_TO_SERVER_RAW,
    // Going from Minecraft to Discord
    MINECRAFT_TO_DISCORD,
    // Going from Discord to Minecraft
    DISCORD_TO_MINECRAFT

}
