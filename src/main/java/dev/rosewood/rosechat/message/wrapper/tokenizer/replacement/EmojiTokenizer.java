package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class EmojiTokenizer implements Tokenizer<ReplacementToken> {

    @Override
    public ReplacementToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input) {
        for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
            if (input.startsWith(emoji.getText())) {
                String groupPermission = messageWrapper.getGroup() == null ? "" : "." + messageWrapper.getGroup().getLocationPermission();
                if (messageWrapper.getLocation() != MessageLocation.NONE
                        && !messageWrapper.getSender().hasPermission("rosechat.emojis." + messageWrapper.getLocation().toString().toLowerCase() + groupPermission)
                        || !messageWrapper.getSender().hasPermission("rosechat.emoji." + emoji.getId())) continue;

                PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(messageWrapper.getSender().getUUID());
                if (playerData == null || !playerData.hasEmojis()) continue;

                String originalContent = input.substring(0, emoji.getText().length());
                String content = emoji.getReplacement();
                return new ReplacementToken(originalContent, content, emoji.getHoverText(), emoji.getFont());
            }
        }

        return null;
    }

}
