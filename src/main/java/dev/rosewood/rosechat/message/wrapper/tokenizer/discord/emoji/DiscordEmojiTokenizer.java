package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class DiscordEmojiTokenizer implements Tokenizer<DiscordEmojiToken> {

    @Override
    public DiscordEmojiToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("<")) {
            Matcher matcher = MessageUtils.DISCORD_EMOJI_PATTERN.matcher(input);
            if (matcher.find()) {
                return new DiscordEmojiToken(messageWrapper.getLocation(), group, sender, viewer, input.substring(matcher.start(), matcher.end()), matcher.group(1));
            }
        }

        return null;
    }
}