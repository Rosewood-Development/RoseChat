package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplacementTokenizer extends Tokenizer {

    public RegexReplacementTokenizer() {
        super("regex_replacement");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (!replacement.isRegex()) continue;
            if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.replacements", "rosechat.replacement." + replacement.getId())) continue;

            Matcher matcher = Pattern.compile(replacement.getText()).matcher(input);
            if (matcher.find()) {
                String originalContent = matcher.group();
                if (!input.startsWith(originalContent)) return null;

                String content = replacement.getReplacement();

                return new TokenizerResult(Token.group(content)
                        .placeholder("message", originalContent)
                        .placeholder("extra", originalContent)
                        .ignoreTokenizer(this)
                        .build(), originalContent.length());
            }
        }

        return null;
    }

}
