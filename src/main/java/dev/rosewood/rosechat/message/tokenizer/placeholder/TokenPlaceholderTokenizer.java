package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenPlaceholderTokenizer extends Tokenizer {

    private static final String REGEX_GROUP_PREFIX = "group_";
    private static final String REGEX_INPUT_PREFIX = "input_";
    private static final Pattern PATTERN = Pattern.compile("%(.*?)%");

    public TokenPlaceholderTokenizer() {
        super("token_placeholder");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();

        List<TokenizerResult> results = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(input);
        while (matcher.find()) {
            int start = matcher.start();
            String rawPlaceholder = matcher.group();
            String placeholder = matcher.group(1);

            StringPlaceholders placeholders = params.getPlaceholders();
            boolean regexInputGroup = placeholder.startsWith(REGEX_INPUT_PREFIX);
            String replacement = placeholders.getPlaceholders().get(placeholder);
            if (replacement == null && regexInputGroup) {
                String inputGroup = REGEX_GROUP_PREFIX + placeholder.substring(REGEX_INPUT_PREFIX.length());
                replacement = placeholders.getPlaceholders().get(inputGroup);
            }

            if (replacement == null)
                continue;

            Token.Builder builder = Token.group(replacement);
            if (regexInputGroup)
                builder.containsPlayerInput();

            boolean ignoreSelf = replacement.contains(rawPlaceholder);
            if (ignoreSelf)
                builder.ignoreTokenizer(this);

            results.add(new TokenizerResult(builder.build(), start, rawPlaceholder.length()));
        }

        return results;
    }

}
