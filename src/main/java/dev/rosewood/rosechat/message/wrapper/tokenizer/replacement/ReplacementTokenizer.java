package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class ReplacementTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex() || !input.startsWith(replacement.getText())) continue;
            if (!this.hasExtendedPermission(messageWrapper, ignorePermissions, "rosechat.replacements", "rosechat.replacement." + replacement.getId())) return null;
            String originalContent = input.substring(0, replacement.getText().length());
            String content = replacement.getReplacement();

            return new Token(new Token.TokenSettings(originalContent).content(content).placeholder("message", originalContent).ignoreTokenizer(this));
        }
        return null;
    }

}
