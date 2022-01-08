package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.italic;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import org.bukkit.Bukkit;

public class DiscordItalicTokenizer implements Tokenizer<DiscordItalicToken> {

    @Override
    public DiscordItalicToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("*") || input.startsWith("_")) {
            int lastIndex = 0;

            char[] chars = input.toCharArray();
            for (int i = 1; i < chars.length; i++) {
                if ((chars[i] == '*' && input.startsWith("*")) || (chars[i] == '_' && input.startsWith("_"))) {
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
