package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class RosechatPlaceholderToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final CustomPlaceholder placeholder;

    public RosechatPlaceholderToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, String originalContent, CustomPlaceholder placeholder) {
        super(sender, viewer, originalContent);
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.placeholder = placeholder;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        BaseComponent[] components = MessageUtils.parseCustomPlaceholder(this.getSender(), this.getSender(), this.placeholder.getId(),
                MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group, this.messageWrapper.getPlaceholders()).build(), false);

        if (components != null) componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);

        return componentBuilder.create();
    }
}
