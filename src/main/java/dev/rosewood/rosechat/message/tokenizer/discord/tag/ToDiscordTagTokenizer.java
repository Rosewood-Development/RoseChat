package dev.rosewood.rosechat.message.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class ToDiscordTagTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!this.hasPermission(roseMessage, ignorePermissions, "rosechat.tag")) return null;
        if (!Setting.CAN_TAG_MEMBERS.getBoolean()) return null;
        if (!input.startsWith("@")) return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        DiscordChatProvider.DetectedMention member = discord.matchPartialMember(input.substring(1));
        if (member == null) return null;

        return new Token(new Token.TokenSettings(input.substring(0, member.getConsumedTextLength() + 1)).content(member.getMention()).ignoreTokenizer(this)
                .ignoreTokenizer(Tokenizers.COLOR).ignoreTokenizer(Tokenizers.FORMAT));
    }

}
