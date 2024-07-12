package dev.rosewood.rosechat.chat.channel;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import java.util.UUID;

public record ChannelMessageOptions(RosePlayer sender, String message, String format,
                                    boolean sendToDiscord, String discordId, UUID messageId,
                                    boolean isJson, RoseMessage wrapper, boolean bypassSlowmode) {

    public static class Builder {

        private RosePlayer sender;
        private String message;
        private String format;
        private boolean sendToDiscord;
        private String discordId;
        private UUID messageId;
        private boolean isJson;
        private RoseMessage wrapper;
        private boolean bypassSlowmode;

        public Builder sender(RosePlayer sender) {
            this.sender = sender;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder sendToDiscord(boolean sendToDiscord) {
            this.sendToDiscord = sendToDiscord;
            return this;
        }

        public Builder discordId(String discordId) {
            this.discordId = discordId;
            return this;
        }

        public Builder messageId(UUID messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder isJson(boolean isJson) {
            this.isJson = isJson;
            return this;
        }

        public Builder wrapper(RoseMessage wrapper) {
            this.wrapper = wrapper;
            return this;
        }

        public Builder bypassSlowmode(boolean bypassSlowmode) {
            this.bypassSlowmode = bypassSlowmode;
            return this;
        }

        public ChannelMessageOptions build() {
            return new ChannelMessageOptions(this.sender, this.message, this.format, this.sendToDiscord, this.discordId,
                    this.messageId, this.isJson, this.wrapper, this.bypassSlowmode);
        }

    }

}
