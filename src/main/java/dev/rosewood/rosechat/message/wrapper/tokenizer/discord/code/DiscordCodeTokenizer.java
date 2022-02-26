package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import org.bukkit.Bukkit;

public class DiscordCodeTokenizer implements Tokenizer<DiscordCodeToken> {

    @Override
    public DiscordCodeToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (!sender.hasPermission("rosechat.discord." + group.getLocationPermission())) return null;
        if (input.startsWith("`")) {
            int lastIndex = 0;

            char[] chars = input.toCharArray();
            for (int i = 1; i < chars.length; i++) {
                if (chars[i] == '`') {
                    lastIndex = i;
                    break;
                }
            }

            if (lastIndex == 0) return null;
            return new DiscordCodeToken(sender, viewer, input.substring(0, lastIndex + 1));
        }

        return null;
    }
}
