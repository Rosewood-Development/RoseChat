package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.quote;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import org.bukkit.Bukkit;

public class DiscordQuoteTokenizer implements Tokenizer<DiscordQuoteToken> {

    @Override
    public DiscordQuoteToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (!sender.hasPermission("rosechat.discord." + group.getLocationPermission())) return null;
        if (input.startsWith("> ")) {
            return new DiscordQuoteToken(sender, viewer, input);
        }

        return null;
    }
}
