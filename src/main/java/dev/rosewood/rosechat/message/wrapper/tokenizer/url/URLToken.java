package dev.rosewood.rosechat.message.wrapper.tokenizer.url;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class URLToken extends Token {

    private final Group group;

    public URLToken(Group group, RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
        this.group = group;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        PlaceholderManager manager = RoseChatAPI.getInstance().getPlaceholderManager();
        CustomPlaceholder placeholder = manager.getPlaceholder("url");
        if (placeholder == null) return null;

        BaseComponent[] component;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group)
                .addPlaceholder("url", this.getOriginalContent()).build();
        component = TextComponent.fromLegacyText(placeholders.apply(placeholder.getText().parse(this.getSender(), this.getViewer(), placeholders)) + "&f&r");

        if (placeholder.getHover() != null) {
            String hoverString = placeholders.apply(placeholder.getHover().parse(this.getSender(), this.getViewer(), placeholders));
            BaseComponent[] hover = new MessageTokenizer(this.group, this.getSender(), this.getViewer(), MessageLocation.OTHER, hoverString, MessageTokenizer.TAG_TOKENIZERS).toComponents();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        if (placeholder.getClick() != null) {
            String clickString = placeholders.apply(placeholder.getClick().parse(this.getSender(), this.getViewer(), placeholders));
            clickString = clickString.startsWith("http") ? clickString : "https://" + clickString;
            ClickEvent.Action action = placeholder.getClick().parseToAction(this.getSender(), this.getViewer(), placeholders);
            clickEvent = new ClickEvent(action, clickString);
        }

        for (BaseComponent c : component) {
            for (char x : c.toPlainText().toCharArray()) {
                componentBuilder.append(String.valueOf(x)).event(hoverEvent).event(clickEvent);
            }
        }

        return componentBuilder.create();
    }
}
