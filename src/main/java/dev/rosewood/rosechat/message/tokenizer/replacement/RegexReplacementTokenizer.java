package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplacementTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (!replacement.isRegex()) continue;
            if (!ignorePermissions
                    && !MessageUtils.hasExtendedTokenPermission(roseMessage, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
                return null;

            Matcher matcher = Pattern.compile(replacement.getText()).matcher(input);
            if (matcher.find()) {
                String originalContent = input.substring(matcher.start(), matcher.end());
                if (!input.startsWith(originalContent)) return null;

                String content = replacement.getReplacement();

                return new Token(new Token.TokenSettings(originalContent).content(content).placeholder("message", originalContent)
                        .placeholder("extra", originalContent).ignoreTokenizer(this));
            }
        }

        return null;
    }

}
