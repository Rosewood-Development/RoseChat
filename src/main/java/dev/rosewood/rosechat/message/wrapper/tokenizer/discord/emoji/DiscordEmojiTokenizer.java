package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;

public class DiscordEmojiTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("<")) return null;
        Matcher matcher = MessageUtils.DISCORD_EMOJI_PATTERN.matcher(input);
        if (matcher.find()) {
            String originalContent = input.substring(matcher.start(), matcher.end());
            String content = matcher.group(1);

            for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
                if (!emoji.getText().equalsIgnoreCase(content)) continue;
                if (!hasExtendedPermission(messageWrapper, ignorePermissions, "rosechat.emojis", "rosechat.emoji." + emoji.getId())) return null;
                content = emoji.getReplacement();

                return new Token(new Token.TokenSettings(originalContent).content(content).font(emoji.getFont())
                        .hoverAction(HoverEvent.Action.SHOW_TEXT).hover(emoji.getHoverText()).ignoreTokenizer(this));
            }

            return new Token(new Token.TokenSettings(originalContent).content(matcher.group(1)));
        }

        return null;
    }

}
