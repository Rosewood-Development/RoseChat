package dev.rosewood.rosechat.message.wrapper.tokenizer.whitespace;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class WhitespaceTokenizer implements Tokenizer<WhitespaceToken> {

    @Override
    public WhitespaceToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                stringBuilder.append(c);
            } else {
                break;
            }
        }

        return stringBuilder.length() == 0 ? null : new WhitespaceToken(sender, viewer, stringBuilder.toString());
    }
}
