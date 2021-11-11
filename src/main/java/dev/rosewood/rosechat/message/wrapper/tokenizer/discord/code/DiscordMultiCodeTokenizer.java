package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import org.bukkit.Bukkit;

public class DiscordMultiCodeTokenizer implements Tokenizer<DiscordMultiCodeToken> {

    @Override
    public DiscordMultiCodeToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("```")) {
            int lastIndex = 0;

            char[] chars = input.toCharArray();
            for (int i = 3; i < chars.length; i++) {
                if (chars.length - 1 > i && chars[i] == '`' && chars[i + 1] == '`' && chars[i + 2] == '`') {
                    lastIndex = i + 2;
                    break;
                }
            }

            if (lastIndex == 0) return null;
            return new DiscordMultiCodeToken(sender, viewer, input.substring(0, lastIndex + 1));
        }

        return null;
    }
}
