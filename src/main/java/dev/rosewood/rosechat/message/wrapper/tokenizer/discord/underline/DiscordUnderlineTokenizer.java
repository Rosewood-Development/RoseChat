package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.underline;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class DiscordUnderlineTokenizer implements Tokenizer<DiscordUnderlineToken> {

    @Override
    public DiscordUnderlineToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (!sender.hasPermission("rosechat.discord." + group.getLocationPermission())) return null;
        if (input.startsWith("__")) {
            int lastIndex = 0;

            char[] chars = input.toCharArray();
            for (int i = 2; i < chars.length; i++) {
                if (chars.length - 1 > i && chars[i] == '_' && chars[i+1] == '_') {
                    lastIndex = i + 1;
                    break;
                }
            }

            if (lastIndex == 0) return null;
            return new DiscordUnderlineToken(sender, viewer, input.substring(0, lastIndex + 1));
        }

        return null;
    }
}
