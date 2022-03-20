package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.tag;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class DiscordTagTokenizer implements Tokenizer<DiscordTagToken> {

    @Override
    public DiscordTagToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (input.startsWith("<")) {
            Matcher matcher = MessageUtils.DISCORD_TAG_PATTERN.matcher(input);
            if (matcher.find()) {
                return new DiscordTagToken(messageWrapper, group, sender, viewer, input.substring(matcher.start(), matcher.end()), matcher.group(1));
            }

            Matcher roleMatcher = MessageUtils.DISCORD_ROLE_TAG_PATTERN.matcher(input);
            if (roleMatcher.find()) {
                return new DiscordTagToken(messageWrapper, group, sender, viewer, input.substring(roleMatcher.start(), roleMatcher.end()), roleMatcher.group(1), true);
            }
        }

        return null;
    }
}
