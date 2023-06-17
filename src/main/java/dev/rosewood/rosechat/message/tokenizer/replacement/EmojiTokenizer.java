package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;

public class EmojiTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
            if (input.startsWith(emoji.getText())) {
                if (!ignorePermissions && !MessageUtils.hasExtendedTokenPermission(roseMessage, "rosechat.emojis", "rosechat.emoji." + emoji.getId()))
                    return null;

                PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(roseMessage.getSender().getUUID());
                if (playerData != null && !playerData.hasEmojis()) continue;

                String originalContent = emoji.getText();
                String content = emoji.getReplacement();

                return new Token(new Token.TokenSettings(originalContent).content(content).hover(emoji.getHoverText()).font(emoji.getFont()).ignoreTokenizer(this));
            }
        }

        return null;
    }

}
