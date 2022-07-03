package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder;

import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPIPlaceholderTokenizer implements Tokenizer<Token> {

    private static final Pattern PAPI_PATTERN = Pattern.compile("\\%(.*?)\\%");

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input) {
        if (!input.startsWith("%")) return null;

        Matcher matcher = PAPI_PATTERN.matcher(input);
        if (matcher.find()) {
            String placeholder = input.substring(matcher.start() + 1, matcher.end() - 1);
            String placeholderPermission = placeholder.replaceFirst("_", ".");
            String groupPermission = messageWrapper.getGroup() == null ? "" : "." + messageWrapper.getGroup().getLocationPermission();
            if ((messageWrapper.getLocation() != MessageLocation.NONE && !messageWrapper.getSender().hasPermission("rosechat.placeholders." + messageWrapper.getLocation().toString().toLowerCase() + groupPermission)) || !messageWrapper.getSender().hasPermission("rosechat.placeholder." + placeholderPermission)) return null;

            String originalContent = input.substring(matcher.start(), matcher.end());
            String content = originalContent.startsWith("%other_") ?
                    PlaceholderAPIHook.applyPlaceholders(viewer.asPlayer(), originalContent.replaceFirst("other_", "")) :
                    PlaceholderAPIHook.applyPlaceholders(messageWrapper.getSender().asPlayer(), originalContent);
            return new Token(new Token.TokenSettings(originalContent).content(content));
        }
        return null;
    }

}
