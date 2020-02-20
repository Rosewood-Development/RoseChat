package me.lilac.rosechat.placeholder;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.utils.Methods;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;

public class PlaceholderMessage {

    private Rosechat plugin;
    private Player sender;
    private Player target;
    private String message;
    private FormatType format;
    private PlaceholderManager manager;

    public PlaceholderMessage(Player sender, String message, FormatType format) {
        this.plugin = Rosechat.getInstance();
        this.sender = sender;
        this.message = message;
        this.format = format;
        manager = plugin.getPlaceholderManager();
    }

    public PlaceholderMessage(Player sender, Player target, String message, FormatType format) {
        this.plugin = Rosechat.getInstance();
        this.sender = sender;
        this.target = target;
        this.message = message;
        this.format = format;
        manager = plugin.getPlaceholderManager();
    }

    public TextComponent getMessage() {
        TextComponent component = new TextComponent();
        for (String placeholder : manager.getOrphanedPlaceholders().get(format)) {
            if (!manager.getPlaceholders().keySet().contains(placeholder))  continue;
            CustomPlaceholder custom = manager.getPlaceholders().get(placeholder);
            if (custom.getText() == null) continue;;

            BaseComponent text = getText(custom.getText());
            HoverEvent hover = null;
            ClickEvent click = null;
            if (custom.getHover() != null) hover = getHover(custom.getHover());
            if (custom.getClick() != null) click = getClick(custom.getClick());

            if (hover != null) text.setHoverEvent(hover);
            if (click != null) text.setClickEvent(click);

            component.addExtra(text);
        }

        return component;
    }

    private BaseComponent getText(TextPlaceholder placeholder) {
        if (!placeholder.shouldUseGroups()) return getDefaultText(placeholder);

        for (String group : placeholder.getGroups().keySet()) {
            if (!plugin.getVault().getPrimaryGroup(sender).equalsIgnoreCase(group)) continue;
            if (placeholder.getGroups().get(group).contains("%message%")) {
                return target == null ?
                        new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, placeholder.getGroups().get(group)
                                .replace("%message%", Methods.format(message)))))
                        : new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, target, placeholder.getGroups().get(group)
                        .replace("%message%", Methods.format(message)))));
            }

            return target == null ?
                    new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, placeholder.getGroups().get(group))))
                    : new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, target, placeholder.getGroups().get(group))));
        }

        return getDefaultText(placeholder);
    }

    private TextComponent getDefaultText(TextPlaceholder placeholder) {
        if (placeholder.getDefaultText().contains("%message%")) {
            return target == null ?
                    new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, placeholder.getDefaultText()
                            .replace("%message%", message))))
                    : new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, target, placeholder.getDefaultText()
                    .replace("%message%", message))));
        }
;
        return target == null ?
                new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, placeholder.getDefaultText())))
                : new TextComponent(TextComponent.fromLegacyText(Methods.format(sender, target, placeholder.getDefaultText())));
    }


    private HoverEvent getHover(HoverPlaceholder placeholder) {
        if (!placeholder.shouldUseGroups()) return getDefaultHover(placeholder);

        for (String group : placeholder.getGroups().keySet()) {
            if (!plugin.getVault().getPrimaryGroup(sender).equalsIgnoreCase(group)) continue;
            TextComponent[] component = new TextComponent[1];
            component[0] = target == null ? new TextComponent(Methods.format(sender, placeholder.getGroups().get(group)))
                    : new TextComponent(Methods.format(sender, target, placeholder.getGroups().get(group)));
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, component);
        }

        return getDefaultHover(placeholder);
    }

    private HoverEvent getDefaultHover(HoverPlaceholder placeholder) {
        TextComponent[] component = new TextComponent[1];
        component[0] = target == null ? new TextComponent(Methods.format(sender, placeholder.getDefaultHoverEvent()))
                : new TextComponent(Methods.format(sender, target, placeholder.getDefaultHoverEvent()));
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, component);
    }

    private ClickEvent getClick(ClickPlaceholder placeholder) {
        if (!placeholder.shouldUseGroups()) return getDefaultClick(placeholder);

        for (String group : placeholder.getGroups().keySet()) {
            if (!plugin.getVault().getPrimaryGroup(sender).equalsIgnoreCase(group)) continue;
            ClickPlaceholder.RoseClickEvent click = placeholder.getGroups().get(group);
            String extra = target == null ? Methods.format(sender, click.getExtra())
                    : Methods.format(sender, target, click.getExtra());
            return new ClickEvent(click.getAction(), extra);
        }

        return getDefaultClick(placeholder);
    }

    private ClickEvent getDefaultClick(ClickPlaceholder placeholder) {
        String extra = target == null ? Methods.format(sender, placeholder.getDefaultClick().getExtra())
                : Methods.format(sender, target, placeholder.getDefaultClick().getExtra());
        return new ClickEvent(placeholder.getDefaultClick().getAction(), extra);
    }
}
