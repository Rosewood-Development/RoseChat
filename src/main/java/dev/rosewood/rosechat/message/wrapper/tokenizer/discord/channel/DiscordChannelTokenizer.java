package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.channel;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class DiscordChannelTokenizer implements Tokenizer<DiscordChannelToken> {

    @Override
    public DiscordChannelToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("<")) {
            Matcher matcher = MessageUtils.DISCORD_CHANNEL_PATTERN.matcher(input);
            if (matcher.find()) {
                return new DiscordChannelToken(messageWrapper, group, sender, viewer, input.substring(matcher.start(), matcher.end()), matcher.group(1));
            }
        }

        return null;
    }
}
