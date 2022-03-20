package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordEmojiToken extends Token {

    private final MessageLocation location;
    private final Group group;
    private final String emoji;

    public DiscordEmojiToken(MessageLocation location, Group group, RoseSender sender, RoseSender viewer, String originalContent, String emoji) {
        super(sender, viewer, originalContent);
        this.location = location;
        this.group = group;
        this.emoji = emoji;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        String content = ":" + this.emoji + ":";

        for (ChatReplacement emoji : RoseChatAPI.getInstance().getEmojis()) {
            String groupPermission = this.group == null ? "" : "." + this.group.getLocationPermission();
            if (this.group != null && this.location != MessageLocation.NONE && !this.getSender().hasPermission("rosechat.emojis." + this.location.toString().toLowerCase() + groupPermission) || !this.getSender().hasPermission("rosechat.emoji." + emoji.getId())) continue;
            if (emoji.getText().equalsIgnoreCase(content)) {
                content = emoji.getReplacement();
                this.setFont(emoji.getFont());
                componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(emoji.getHoverText())));
            }
        }

        for (char c : content.toCharArray()) {
            componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
        }

        return componentBuilder.create();
    }
}
