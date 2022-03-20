package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RosechatPlaceholderTokenizer implements Tokenizer<RosechatPlaceholderToken> {

    private static final Pattern RC_PATTERN = Pattern.compile("\\{(.*?)\\}");

    @Override
    public RosechatPlaceholderToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (!input.startsWith("{")) return null;

        Matcher matcher = RC_PATTERN.matcher(input);
        if (matcher.find()) {
            String placeholder = input.substring(matcher.start() + 1, matcher.end() - 1);
            String groupPermission = group == null ? "" : "." + group.getLocationPermission();
            if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.placeholders." + location.toString().toLowerCase() + groupPermission)
                    || !sender.hasPermission("rosechat.placeholder.rosechat." + placeholder)) return null;

            CustomPlaceholder customPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholder);
            if (customPlaceholder == null) return null;
            return new RosechatPlaceholderToken(wrapper, group, sender, viewer, input.substring(matcher.start(), matcher.end()), customPlaceholder);
        }

        return null;
    }
}
