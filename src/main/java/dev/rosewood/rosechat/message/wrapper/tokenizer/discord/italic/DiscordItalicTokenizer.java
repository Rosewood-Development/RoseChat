package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.italic;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class DiscordItalicTokenizer implements Tokenizer<DiscordItalicToken> {

    @Override
    public DiscordItalicToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("*")) {
            int lastIndex = 0;

            char[] chars = input.toCharArray();
            for (int i = 2; i < chars.length; i++) {
                if (chars.length - 1 > i && chars[i] == '*') {
                    lastIndex = i;
                    break;
                }
            }

            if (lastIndex == 0) return null;
            return new DiscordItalicToken(sender, viewer, input.substring(0, lastIndex + 1));
        }

        return null;
    }
}
