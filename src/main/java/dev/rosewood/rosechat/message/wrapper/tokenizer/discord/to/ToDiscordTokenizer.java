package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.to;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class ToDiscordTokenizer implements Tokenizer<ToDiscordToken> {

    @Override
    public ToDiscordToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        /**
         * convert color codes to discord markdown
         */
        return null;
    }
}
