package dev.rosewood.rosechat.message.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;

public class ToDiscordTagTokenizer extends Tokenizer {

    public ToDiscordTagTokenizer() {
        super("discord_tag");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        if (!Settings.CAN_TAG_MEMBERS.get())
            return null;

        if (!input.startsWith("@"))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.tag"))
            return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        DiscordChatProvider.DetectedMention member = discord.matchPartialMember(input.substring(1));
        if (member == null)
            return null;

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return new TokenizerResult(Token.text(input), input.length() + 1);

        return new TokenizerResult(Token.text(member.mention()), member.consumedTextLength() + 1);
    }

}
