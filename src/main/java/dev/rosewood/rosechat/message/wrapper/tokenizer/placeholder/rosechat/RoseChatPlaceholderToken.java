package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class RoseChatPlaceholderToken extends Token {

    private final CustomPlaceholder placeholder;

    public RoseChatPlaceholderToken(String originalContent, CustomPlaceholder placeholder) {
        super(originalContent);
        this.placeholder = placeholder;
    }

    @Override
    public String getContent(MessageWrapper wrapper, RoseSender viewer) {
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(wrapper.getSender(), viewer, wrapper.getGroup(), wrapper.getPlaceholders()).build();
        String content = this.placeholder.getText().parse(wrapper.getSender(), viewer, placeholders);
        return placeholders.apply(content); // this needs to be tokenized
    }

    @Override
    public boolean requiresTokenizing() {
        return true;
    }

    @Override
    public String getHover(MessageWrapper wrapper, RoseSender viewer) {
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(wrapper.getSender(), viewer, wrapper.getGroup(), wrapper.getPlaceholders()).build();
        return this.placeholder.getHover() != null ? placeholders.apply(this.placeholder.getHover().parse(wrapper.getSender(), viewer, placeholders)) : null;
        // this needs to be tokenized too :rofl:
    }

    @Override
    public HoverEvent.Action getHoverAction(MessageWrapper wrapper, RoseSender viewer) {
        return HoverEvent.Action.SHOW_TEXT;
    }

    @Override
    public String getClick(MessageWrapper wrapper, RoseSender viewer) {
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(wrapper.getSender(), viewer, wrapper.getGroup(), wrapper.getPlaceholders()).build();
        return this.placeholder.getClick() != null ? placeholders.apply(this.placeholder.getClick().parse(wrapper.getSender(), viewer, placeholders)) : null;
        // this needs to be tokenized too :rofl:
    }

    @Override
    public ClickEvent.Action getClickAction(MessageWrapper wrapper, RoseSender viewer) {
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(wrapper.getSender(), viewer, wrapper.getGroup(), wrapper.getPlaceholders()).build();
        return this.placeholder.getClick() != null ? this.placeholder.getClick().parseToAction(wrapper.getSender(), viewer, placeholders) : null;
    }

}
