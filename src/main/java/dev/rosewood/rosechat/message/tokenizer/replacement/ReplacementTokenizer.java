package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;

public class ReplacementTokenizer extends Tokenizer {

    public ReplacementTokenizer() {
        super("replacement");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex() || !params.getInput().startsWith(replacement.getText())) continue;
            if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
                return null;

            String originalContent = replacement.getText();
            String content = replacement.getReplacement();

            return new TokenizerResult(Token.group(content).placeholder("message", originalContent).ignoreTokenizer(this).build(), originalContent.length());
        }

        return null;
    }

}
