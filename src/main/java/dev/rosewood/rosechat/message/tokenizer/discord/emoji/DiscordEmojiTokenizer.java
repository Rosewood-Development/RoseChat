package dev.rosewood.rosechat.message.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.HoverEvent;

public class DiscordEmojiTokenizer extends Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("<a?(:[a-zA-Z0-9_\\-~]+:)[0-9]{18,19}>");

    public DiscordEmojiTokenizer() {
        super("discord_emoji");
    }

    // TODO: May need changes with new replacements

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("<")) return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        String content = matcher.group(1);
        for (Replacement emoji : RoseChatAPI.getInstance().getReplacements()) {
            if (!emoji.getInput().getText().equalsIgnoreCase(content)) continue;
            if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.emojis", "rosechat.emoji" + emoji.getId()))
                return null;

            content = emoji.getOutput().getText();

            return new TokenizerResult(Token.group(content)
                    .decorate(FontDecorator.of(emoji.getOutput().getFont()))
                    .decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, emoji.getOutput().getHover()))
                    .ignoreTokenizer(this)
                    .ignoreTokenizer(Tokenizers.REPLACEMENT)
                    .build(), matcher.group().length());
        }

        return new TokenizerResult(Token.text(matcher.group(1)).build(), matcher.group().length());
    }

}
