package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPIPlaceholderTokenizer implements Tokenizer {

    private static final Pattern PAPI_PATTERN = Pattern.compile("%(.*?)%");
    private final boolean isBungee;

    public PAPIPlaceholderTokenizer(boolean isBungee) {
        this.isBungee = isBungee;
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("%")) return null;

        Matcher matcher = PAPI_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String placeholder = input.substring(1, matcher.end() - 1);
            String placeholderPermission = placeholder.replaceFirst("_", ".");
            if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.placeholders", "rosechat.placeholder." + placeholderPermission))
                return null;

            String originalContent = matcher.group();

            String content;
            if (originalContent.startsWith("%other_") && !this.isBungee) {
                if (!params.getReceiver().isPlayer()) content = originalContent;
                else content = PlaceholderAPIHook.applyPlaceholders(params.getReceiver().asPlayer(), originalContent.replaceFirst("other_", ""));
            } else {
                content = PlaceholderAPIHook.applyPlaceholders(params.getSender().asPlayer(), originalContent);
            }

            if (Objects.equals(content, originalContent)) {
                return new TokenizerResult(Token.text(content).build(), originalContent.length());
            } else {
                return new TokenizerResult(Token.group(content).build(), originalContent.length());
            }
        }

        return null;
    }

}
