package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

public class ReplacementToken extends Token {

    private final Group group;
    private final ChatReplacement replacement;

    public ReplacementToken(RoseSender sender, RoseSender viewer, Group group, ChatReplacement replacement, String originalContent) {
        super(sender, viewer, originalContent);
        this.group = group;
        this.replacement = replacement;
    }

    @Override
    public BaseComponent[] toComponents() {
        String replacement = this.replacement.getReplacement();
        replacement = HexUtils.colorify(replacement.replace("%message%", this.getOriginalContent()));

        PlaceholderManager manager = RoseChatAPI.getInstance().getPlaceholderManager();
        CustomPlaceholder placeholder = manager.getPlaceholder(replacement.replace("{", "").replace("}", ""));

        ComponentBuilder componentBuilder = new ComponentBuilder();

        if (placeholder  != null) {
            StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group)
                    .addPlaceholder("message", this.getOriginalContent()).build();

            String text = placeholder.getText().parse(this.getSender(), this.getViewer(), placeholders);
            BaseComponent[] component = new MessageTokenizer(this.group, this.getSender(), this.getViewer(), MessageLocation.OTHER, HexUtils.colorify(placeholders.apply(text)), MessageTokenizer.REPLACEMENT_TOKENIZERS).toComponents();
            HoverEvent hoverEvent = null;
            ClickEvent clickEvent = null;

            if (placeholder.getHover() == null) {
                if (this.replacement.getHoverText() != null)
                    componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(HexUtils.colorify(this.replacement.getHoverText()))));
            } else {
                String hoverString = placeholders.apply(placeholder.getHover().parse(this.getSender(), this.getViewer(), placeholders));
                if (hoverString != null) {
                    BaseComponent[] hover = new MessageTokenizer(this.group, this.getSender(), this.getViewer(), MessageLocation.OTHER, hoverString, MessageTokenizer.REPLACEMENT_TOKENIZERS).toComponents();
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
                }
            }

            String clickString = placeholder.getClick() != null ? placeholders.apply(placeholder.getClick().parse(this.getSender(), this.getViewer(), placeholders)) : null;
            ClickEvent.Action action = placeholder.getClick() != null ? placeholder.getClick().parseToAction(this.getSender(), this.getViewer(), placeholders) : null;
            if (clickString != null && action != null) {
                String click = this.getSender().isPlayer() ? PlaceholderAPIHook.applyPlaceholders(this.getSender().asPlayer(), clickString) : clickString;
                if (click != null) {
                    click = click.startsWith("http") ? click : "https://" + click;
                    clickEvent = new ClickEvent(action, click);
                }
            }

            for (BaseComponent c : component) {
                componentBuilder.append(c.toPlainText()).event(hoverEvent).event(clickEvent);
            }
        } else {
            for (char c : replacement.toCharArray()) {
                componentBuilder.append(HexUtils.colorify(c + "&f")).font(this.replacement.getFont());
                if (this.replacement.getHoverText() != null)
                    componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(HexUtils.colorify(this.replacement.getHoverText()))));
            }
        }


        return componentBuilder.create();
    }

    public String getFont() {
        return this.replacement.getFont();
    }
}
