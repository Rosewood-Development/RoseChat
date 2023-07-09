package dev.rosewood.rosechat.message.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
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
        String input = params.getInput();
        if (!Setting.CAN_TAG_MEMBERS.getBoolean()) return null;
        if (!input.startsWith("@")) return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.tag")) return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        DiscordChatProvider.DetectedMention member = discord.matchPartialMember(input.substring(1));
        if (member == null) return null;

        return new TokenizerResult(Token.text(member.mention()).build(), member.consumedTextLength() + 1);
    }

}
