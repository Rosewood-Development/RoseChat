package dev.rosewood.rosechat.message.wrapper.tokenizer.url;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class URLToken extends Token {

    private final Group group;
    private final String display;
    private final String url;

    public URLToken(RoseSender sender, RoseSender viewer, Group group, String originalContent, String display, String url) {
        super(sender, viewer, originalContent);
        this.group = group;
        this.display = display;
        this.url = url;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        PlaceholderManager placeholderManager = RoseChatAPI.getInstance().getPlaceholderManager();
        CustomPlaceholder placeholder = placeholderManager.getPlaceholder("url");

        if (placeholder == null) {
            for (char c : this.display.toCharArray()) {
                componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, this.url.startsWith("http") ? this.url : "https://" + this.url));
            }
        } else {
            StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group)
                    .addPlaceholder("message", this.display).build();

            String text = placeholder.getText().parse(this.getSender(), this.getViewer(), placeholders);
            text = placeholders.apply(text) + "&f&r";

            HoverEvent hoverEvent = null;
            if (placeholder.getHover() != null) {
                String hoverString = placeholders.apply(placeholder.getHover().parse(this.getSender(), this.getViewer(), placeholders));
                if (hoverString != null) {
                    MessageTokenizer hoverTokenizer = new MessageTokenizer.Builder()
                            .group(this.group).sender(this.getSender()).viewer(this.getViewer()).location(MessageLocation.OTHER)
                            .tokenizers(Tokenizers.REPLACEMENT_TOKENIZERS).tokenize(hoverString);
                    BaseComponent[] hover = hoverTokenizer.toComponents();
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
                }
            }

            for (char c : text.toCharArray()) {
                componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE)
                        .event(hoverEvent)
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, this.url.startsWith("http") ? this.url : "https://" + this.url));
            }
        }

        return componentBuilder.create();
    }
}
