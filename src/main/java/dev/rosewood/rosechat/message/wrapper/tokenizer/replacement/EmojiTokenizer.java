package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class EmojiTokenizer implements Tokenizer<ReplacementToken> {

    @Override
    public ReplacementToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(sender.getUUID());

        if (playerData == null || playerData.hasEmojis()) {
            for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
                String groupPermission = group == null ? "" : "." + group.getLocationPermission();
                if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.emojis." + location.toString().toLowerCase() + groupPermission) || !sender.hasPermission("rosechat.emoji." + emoji.getId())) continue;
                if (input.startsWith(emoji.getText())) {
                    return new ReplacementToken(sender, viewer, group, emoji, input.substring(0, emoji.getText().length()));
                }
            }
        }

        return null;
    }
}
