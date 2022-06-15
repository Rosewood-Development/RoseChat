package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi;

import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPIPlaceholderTokenizer implements Tokenizer<PAPIPlaceholderToken> {

    private static final Pattern PAPI_PATTERN = Pattern.compile("\\%(.*?)\\%");

    @Override
    public PAPIPlaceholderToken tokenize(MessageWrapper messageWrapper, String input) {
        if (!input.startsWith("%")) return null;

        Matcher matcher = PAPI_PATTERN.matcher(input);
        if (matcher.find()) {
            String placeholder = input.substring(matcher.start() + 1, matcher.end() - 1);
            String placeholderPermission = placeholder.replaceFirst("_", ".");
            String groupPermission = messageWrapper.getGroup() == null ? "" : "." + messageWrapper.getGroup().getLocationPermission();
            if ((messageWrapper.getLocation() != MessageLocation.NONE && !messageWrapper.getSender().hasPermission("rosechat.placeholders." + messageWrapper.getLocation().toString().toLowerCase() + groupPermission)) || !messageWrapper.getSender().hasPermission("rosechat.placeholder." + placeholderPermission)) return null;
            return new PAPIPlaceholderToken(input.substring(matcher.start(), matcher.end()));
        }
        return null;
    }

}
