package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.from;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Pattern;

public class FromDiscordTokenizer implements Tokenizer<FromDiscordToken> {

    private static final Pattern DISCORD_MARKDOWN_PATTERN = Pattern.compile("((`){1,3}|(\\*){1,3}|(~){2}|(\\|){2}|^(>){1,3}|(_){1,2})+");

    @Override
    public FromDiscordToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        /**
         * if matcher.find
         * translate discord markdown to rc config
         */
        return null;
    }
}
