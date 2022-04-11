package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.tag;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class ToDiscordTagTokenizer implements Tokenizer<ToDiscordTagToken> {

    @Override
    public ToDiscordTagToken tokenize(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        if (!Setting.CAN_TAG_MEMBERS.getBoolean()) return null;
        if (input.startsWith("@")) {
            int space = input.indexOf(" ");

            return new ToDiscordTagToken(sender, viewer, input.contains(" ") ? input.substring(0, space) : input);
        }

        return null;
    }
}
