package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.format;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class ToDiscordFormattingTokenizer implements Tokenizer<ToDiscordFormattingToken> {

    @Override
    public ToDiscordFormattingToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("&")){
            return new ToDiscordFormattingToken(sender, viewer, input);
        }

        return null;
    }
}
