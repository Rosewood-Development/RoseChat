package dev.rosewood.rosechat.api.event.message.discord;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

public class PreParseDiscordMessageEvent extends DiscordMessageEvent {

    /**
     * Called when a message is about to be parsed and sent to Discord. Useful for editing the message.
     * @param message The {@link RoseMessage} for the message that will be parsed.
     * @param textChannel The {@link TextChannel} where the message will be sent.
     */
    public PreParseDiscordMessageEvent(RoseMessage message, TextChannel textChannel) {
        super(message, textChannel);
    }

}
