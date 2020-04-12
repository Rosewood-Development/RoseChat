package dev.rosewood.rosechat.floralapi.root.utils;

public enum Language {

    PREFIX("prefix"),
    NO_PERMISSION("no-permission"),
    PLAYER_NOT_FOUND("player-not-found"),
    PLAYER_ONLY("player-only"),
    RELOADED("reloaded"),
    INVALID_ARGUMENTS("invalid-arguments"),
    COLOR("command-color"),
    COMMAND_RELOAD_DESCRIPTION("command-reload-description");

    private String node;

    Language(String node) {
        this.node = node;
    }

    public String get() {
        return new LocalizedText(node).withPrefixPlaceholder().format();
    }
}
