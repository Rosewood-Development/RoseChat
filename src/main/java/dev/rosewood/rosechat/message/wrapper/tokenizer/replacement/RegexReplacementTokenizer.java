package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplacementTokenizer implements Tokenizer<ReplacementToken> {

    @Override
    public ReplacementToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (!replacement.isRegex()) continue;
            if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.replacements." + location.toString().toLowerCase()) || !sender.hasPermission("rosechat.replacement." + replacement.getId())) continue;
            Matcher matcher = Pattern.compile(replacement.getText()).matcher(input);
            if (matcher.find()) {
                String found = input.substring(matcher.start(), matcher.end());
                if (!input.startsWith(found)) return null;
                return new ReplacementToken(sender, viewer, replacement, input.substring(matcher.start(), matcher.end()));
            }
        }

        return null;
    }
}
