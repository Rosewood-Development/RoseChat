package dev.rosewood.rosechat.message.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;

public class DiscordEmojiTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("<")) return null;
        
        Matcher matcher = MessageUtils.DISCORD_EMOJI_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String originalContent = input.substring(0, matcher.end());
            String content = matcher.group(1);

            for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
                if (!emoji.getText().equalsIgnoreCase(content)) continue;
                if (!ignorePermissions && !MessageUtils.hasExtendedTokenPermission(roseMessage, "rosechat.emojis", "rosechat.emoji" + emoji.getId()))
                    return null;

                content = emoji.getReplacement();

                return new Token(new Token.TokenSettings(originalContent).content(content).font(emoji.getFont())
                        .hoverAction(HoverEvent.Action.SHOW_TEXT).hover(emoji.getHoverText()).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.EMOJI));
            }

            return new Token(new Token.TokenSettings(originalContent).content(matcher.group(1)));
        }

        return null;
    }

}
