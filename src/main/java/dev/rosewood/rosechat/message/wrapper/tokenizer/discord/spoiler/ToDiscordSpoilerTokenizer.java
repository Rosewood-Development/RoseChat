package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class ToDiscordSpoilerTokenizer implements Tokenizer<ToDiscordSpoilerToken> {

    @Override
    public ToDiscordSpoilerToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        String spoiler = Setting.DISCORD_FORMAT_SPOILER.getString();
        String prefix = spoiler.substring(0, spoiler.indexOf("%message%"));
        String suffix = spoiler.substring(spoiler.indexOf("%message%") + "%message%".length());
        if (input.startsWith(prefix)) {
            return new ToDiscordSpoilerToken(sender, viewer, input.substring(0, input.indexOf(suffix) + suffix.length()), input.replace(prefix, "").replace(suffix, ""));
        }

        return null;
    }
}
