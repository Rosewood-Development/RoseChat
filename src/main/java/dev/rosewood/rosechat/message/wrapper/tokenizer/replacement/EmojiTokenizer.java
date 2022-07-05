package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;

public class EmojiTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
            if (input.startsWith(emoji.getText())) {
                String groupPermission = messageWrapper.getGroup() == null ? "" : "." + messageWrapper.getGroup().getLocationPermission();
                if (!ignorePermissions && (messageWrapper.getLocation() != MessageLocation.NONE
                        && !messageWrapper.getSender().hasPermission("rosechat.emojis." + messageWrapper.getLocation().toString().toLowerCase() + groupPermission)
                        || !messageWrapper.getSender().hasPermission("rosechat.emoji." + emoji.getId()))) continue;

                PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(messageWrapper.getSender().getUUID());
                if (playerData == null || !playerData.hasEmojis()) continue;

                String originalContent = input.substring(0, emoji.getText().length());
                String content = emoji.getReplacement();
                return new Token(new Token.TokenSettings(originalContent).content(content).hover(emoji.getHoverText()).font(emoji.getFont()).ignoreTokenizer(this));
            }
        }

        return null;
    }

}
