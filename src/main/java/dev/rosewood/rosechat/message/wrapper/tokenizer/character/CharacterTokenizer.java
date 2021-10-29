package dev.rosewood.rosechat.message.wrapper.tokenizer.character;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class CharacterTokenizer implements Tokenizer<CharacterToken> {

    @Override
    public CharacterToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        return new CharacterToken(sender, viewer, String.valueOf(input.charAt(0)));
    }
}
