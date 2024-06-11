package dev.rosewood.rosechat.api.event.message.discord;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

public class PostParseDiscordMessageEvent extends DiscordMessageEvent {

    private String output;

    /**
     * Called after a discord message has been parsed.
     * @param message The {@link RoseMessage} for the message that was parsed.
     * @param textChannel The {@link TextChannel} that the message is being sent in.
     * @param output The output of the message after it has been parsed.
     */
    public PostParseDiscordMessageEvent(RoseMessage message, TextChannel textChannel, String output) {
        super(message, textChannel);

        this.output = output;
    }

    /**
     * @return The output of the parsed message.
     */
    public String getOutput() {
        return this.output;
    }

    /**
     * Sets the output of the parsed message.
     * @param output The output to use.
     */
    public void setOutput(String output) {
        this.output = output;
    }

}
