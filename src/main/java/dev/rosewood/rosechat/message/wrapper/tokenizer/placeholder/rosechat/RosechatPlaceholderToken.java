package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;

public class RosechatPlaceholderToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final CustomPlaceholder placeholder;

    public RosechatPlaceholderToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, CustomPlaceholder placeholder) {
        super(sender, viewer, "{" + placeholder.getId() + "}");
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.placeholder = placeholder;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        BaseComponent[] components = MessageUtils.parseCustomPlaceholder(this.getSender(), this.getSender(), this.placeholder.getId(),
                MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group, this.messageWrapper.getPlaceholders()).build());

        for (BaseComponent component : components) {
            if (component.getExtra() != null) {
                for (BaseComponent extra : component.getExtra()) {
                    if (extra == null || extra.toPlainText() == null || extra.toPlainText().length() == 0) continue;
                    for (char c : extra.toPlainText().toCharArray()) {
                        componentBuilder.append(String.valueOf(c))
                                .font(component.getFont())
                                .color(extra.getColor())
                                .event(component.getHoverEvent())
                                .event(component.getClickEvent())
                                .obfuscated(component.isObfuscated())
                                .bold(component.isBold())
                                .underlined(component.isUnderlined())
                                .strikethrough(component.isStrikethrough())
                                .italic(component.isItalic());
                    }
                }
            }
        }

        return componentBuilder.create();
    }
}
