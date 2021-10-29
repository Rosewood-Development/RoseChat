package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class ReplacementTokenizer implements Tokenizer<ReplacementToken> {

    @Override
    public ReplacementToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
            if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.emojis." + location.toString().toLowerCase()) || !sender.hasPermission("rosechat.emoji." + emoji.getId())) continue;
            if (input.startsWith(emoji.getText())) {
                return new ReplacementToken(sender, viewer, emoji, input.substring(0, emoji.getText().length()));
            }
        }

        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex()) continue;
            if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.replacements." + location.toString().toLowerCase()) || !sender.hasPermission("rosechat.replacement." + replacement.getId())) continue;
            if (input.startsWith(replacement.getText())) {
                return new ReplacementToken(sender, viewer, replacement, input.substring(0, replacement.getText().length()));
            }
        }

        return null;
    }
}
