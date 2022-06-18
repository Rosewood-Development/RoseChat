package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.GenericToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplacementTokenizer implements Tokenizer<GenericToken> {

    @Override
    public GenericToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (!replacement.isRegex()) continue;

            String groupPermission = messageWrapper.getGroup() == null ? "" : "." + messageWrapper.getGroup().getLocationPermission();
            if (messageWrapper.getLocation() != MessageLocation.NONE
                    && !messageWrapper.getSender().hasPermission("rosechat.replacements." + messageWrapper.getLocation().toString().toLowerCase() + groupPermission)
                    || !messageWrapper.getSender().hasPermission("rosechat.replacement." + replacement.getId())) continue;

            Matcher matcher = Pattern.compile(replacement.getText()).matcher(input);
            if (matcher.find()) {
                String originalContent = input.substring(matcher.start(), matcher.end());
                if (!input.startsWith(originalContent)) return null;

                String content = replacement.getReplacement();
                return new GenericToken(originalContent, content);
            }
        }

        return null;
    }

}
