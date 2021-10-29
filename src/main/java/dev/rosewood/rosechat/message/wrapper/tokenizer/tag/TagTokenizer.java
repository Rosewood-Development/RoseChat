package dev.rosewood.rosechat.message.wrapper.tokenizer.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;

public class TagTokenizer implements Tokenizer<TagToken> {

    @Override
    public TagToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        for (Tag tag : RoseChatAPI.getInstance().getTags()) {
            if (input.startsWith(tag.getPrefix())) {
                if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.tags." + location.toString().toLowerCase()) || !sender.hasPermission("rosechat.tag." + tag.getId())) continue;
                if (tag.getSuffix() != null && input.endsWith(tag.getSuffix())) {
                    return new TagToken(wrapper, group, sender, viewer, tag, input);
                }

                return new TagToken(wrapper, group, sender, viewer, tag, input.substring(0, input.contains(" ") ? input.indexOf(" ") : input.length()));
            }
        }
        return null;
    }
}
