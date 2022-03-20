package dev.rosewood.rosechat.message.wrapper.tokenizer.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagTokenizer implements Tokenizer<TagToken> {

    @Override
    public TagToken tokenize(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String input) {
        for (Tag tag : RoseChatAPI.getInstance().getTags()) {
            if (input.startsWith(tag.getPrefix())) {
                String groupPermission = group == null ? "" : "." + group.getLocationPermission();
                if (location != MessageLocation.NONE && !sender.hasPermission("rosechat.tags." + location.toString().toLowerCase() + groupPermission) || !sender.hasPermission("rosechat.tag." + tag.getId())) continue;
                if (tag.getSuffix() != null) {
                    Matcher matcher = Pattern.compile(tag.getSuffix()).matcher(input);

                    if (matcher.find()) {
                        return new TagToken(wrapper, group, sender, viewer, tag, input.substring(0, matcher.end()), input.substring(tag.getPrefix().length(), matcher.end() - tag.getSuffix().length()));
                    }
                }

                int endIndex = input.contains(" ") ? input.indexOf(" ") : input.length();
                return new TagToken(wrapper, group, sender, viewer, tag, input.substring(0, endIndex),
                        input.substring(tag.getPrefix().length() - 1, endIndex));
            }
        }
        return null;
    }
}
