package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.format;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatPlaceholderToken;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordFormattingTokenizer implements Tokenizer<DiscordFormattingToken> {

    private static final Pattern RC_PATTERN = Pattern.compile("\\{(.*?)\\}");

    @Override
    public DiscordFormattingToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (!input.startsWith("{")) return null;

        Matcher matcher = RC_PATTERN.matcher(input);
        if (matcher.find()) {
            String placeholder = input.substring(matcher.start(), matcher.end());
            CustomPlaceholder customPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholder.substring(1, placeholder.length() - 1));
            if (customPlaceholder == null) return null;
            return new DiscordFormattingToken(wrapper, group, sender, viewer, customPlaceholder);
        }

        return null;
    }
}
