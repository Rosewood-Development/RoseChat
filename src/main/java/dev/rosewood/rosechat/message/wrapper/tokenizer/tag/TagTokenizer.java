package dev.rosewood.rosechat.message.wrapper.tokenizer.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagTokenizer implements Tokenizer<TagToken> {

    @Override
    public TagToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input) {
        for (Tag tag : RoseChatAPI.getInstance().getTags()) {
            if (input.startsWith(tag.getPrefix())) {
                String groupPermission = messageWrapper.getGroup() == null ? "" : "." + messageWrapper.getGroup().getLocationPermission();
                if (messageWrapper.getLocation() != MessageLocation.NONE
                        && !messageWrapper.getSender().hasPermission("rosechat.tags." + messageWrapper.getLocation().toString().toLowerCase() + groupPermission)
                        || !messageWrapper.getSender().hasPermission("rosechat.tag." + tag.getId())) continue;

                if (tag.getSuffix() != null) {
                    if (!input.contains(tag.getSuffix())) continue;
                    int endIndex = input.lastIndexOf(tag.getSuffix()) + tag.getSuffix().length();
                    String originalContent = input.substring(0, endIndex);
                    String content = input.substring(tag.getPrefix().length(), input.lastIndexOf(tag.getSuffix()));
                    return this.createTagToken(messageWrapper, viewer, originalContent, content, tag);
                }

                int endIndex = input.contains(" ") ? input.indexOf(" ") : input.length();
                String originalContent = input.substring(0, endIndex);
                String content = input.substring(tag.getPrefix().length() - 1, endIndex);
                return this.createTagToken(messageWrapper, viewer, originalContent, content, tag);
            }
        }

        return null;
    }

    private TagToken createTagToken(MessageWrapper wrapper, RoseSender viewer, String originalContent, String content, Tag tag) {
        CustomPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(tag.getFormat());
        if (placeholder == null) return null;

        String punctuation = "";
        if (tag.getSuffix() == null) {
            Matcher matcher = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}]").matcher(content);
            punctuation = matcher.find() ? content.substring(matcher.start() + 1) : "";
            content = content.replace(punctuation, "");
        }

        Player taggedPlayer = MessageUtils.getPlayer(content);
        if (taggedPlayer != null) wrapper.getTaggedPlayers().add(taggedPlayer.getUniqueId());
        if (tag.getSound() != null) wrapper.setTagSound(tag.getSound());

        RoseSender tagged = taggedPlayer != null && tag.shouldTagOnlinePlayers() ? new RoseSender(taggedPlayer) : new RoseSender(content, "default");
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(wrapper.getSender(), tagged, wrapper.getGroup())
                .addPlaceholder("tagged", content).build();

        String hover = placeholder.getHover() == null ? null : placeholders.apply(placeholder.getHover().parse(wrapper.getSender(), viewer, placeholders));

        StringBuilder contentBuilder = new StringBuilder();
        content = placeholder.getText().parse(wrapper.getSender(), tagged, placeholders);
        if (tag.shouldMatchLength()) {
            if (hover != null) {
                String colorlessHover = ChatColor.stripColor(HexUtils.colorify(hover));
                for (int i = 0; i < colorlessHover.length(); i++) contentBuilder.append(content);
            }
        } else {
            contentBuilder.append(content);
            contentBuilder.append(punctuation);
        }

        content = contentBuilder.toString();

        String click = placeholder.getClick() == null ? null : placeholders.apply(placeholder.getClick().parse(wrapper.getSender(), viewer, placeholders));
        ClickEvent.Action clickAction = placeholder.getClick() == null ? null : placeholder.getClick().parseToAction(wrapper.getSender(), viewer, placeholders);

        return new TagToken(originalContent, content, hover, click, clickAction);
    }

}
