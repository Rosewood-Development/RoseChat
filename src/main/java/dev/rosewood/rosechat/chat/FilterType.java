package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;

public enum FilterType {
    CAPS("blocked-caps"),
    SPAM("blocked-spam"),
    URL("blocked-url"),
    SWEAR("blocked-language");

    private String warning;

    FilterType(String warning) {
        this.warning = warning;
    }

    public LocalizedText getWarning() {
        return new LocalizedText(warning).withPrefixPlaceholder();
    }
}
