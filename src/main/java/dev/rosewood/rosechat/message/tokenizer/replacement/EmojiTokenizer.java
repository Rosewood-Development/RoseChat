package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import net.md_5.bungee.api.chat.HoverEvent;

public class EmojiTokenizer extends Tokenizer {

    public EmojiTokenizer() {
        super("emoji");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
            if (input.startsWith(emoji.getText())) {
                if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.emojis", "rosechat.emoji." + emoji.getId()))
                    return null;

                PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(params.getSender().getUUID());
                if (playerData != null && !playerData.hasEmojis()) continue;

                String originalContent = emoji.getText();
                String content = emoji.getReplacement();

                return new TokenizerResult(Token.group(content)
                        .decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, emoji.getHoverText()))
                        .decorate(FontDecorator.of(emoji.getFont()))
                        .ignoreTokenizer(this)
                        .build(), originalContent.length());
            }
        }

        return null;
    }

}
