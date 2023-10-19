package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.message.PostParseMessageEvent;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MessageListener implements Listener {

    @EventHandler
    public void onPostParseMessage(PostParseMessageEvent event) {
        if (event.getMessageDirection() == MessageDirection.TO_DISCORD || event.getMessageDirection() == MessageDirection.TO_BUNGEE_SERVER) return;
        if (event.getViewer().isConsole()) return;

        String permission;
        String format;

        // If the sender is the same as the viewer (player looking at their own message)
        if (event.getMessage().getSender().isPlayer() && event.getMessage().getSender().getUUID().equals(event.getViewer().getUUID())) {
            permission = "rosechat.deletemessages.self";
            format  = Setting.DELETE_OWN_MESSAGE_FORMAT.getString();
        } else {
            permission = "rosechat.deletemessages.others";
            format = Setting.DELETE_OTHER_MESSAGE_FORMAT.getString();
        }

        if (!event.getViewer().hasPermission(permission))
            return;

        BaseComponent[] components = event.getMessageComponents().content();
        if (components != null && components.length > 0)
            this.appendButton(event, components, format);
    }

    private void appendButton(PostParseMessageEvent event, BaseComponent[] components, String placeholder) {
        BaseComponent[] deleteButton = this.getButton(event, placeholder);

        ComponentBuilder componentBuilder = new ComponentBuilder();
        if (Setting.DELETE_MESSAGE_FORMAT_APPEND_SUFFIX.getBoolean()) {
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
            if (deleteButton != null) componentBuilder.append(deleteButton);
        } else {
            if (deleteButton != null) componentBuilder.append(deleteButton);
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
        }

        event.setMessageComponents(new MessageTokenizerResults<>(componentBuilder.create(), event.getMessageComponents().outputs()));
    }

    private BaseComponent[] getButton(PostParseMessageEvent event, String placeholder) {
        return RoseChatAPI.getInstance().parse(new RosePlayer(Bukkit.getConsoleSender()), event.getViewer(), placeholder,
                MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer())
                        .add("id", event.getMessage().getUUID())
                        .add("type", "server")
                        .add("channel", event.getMessage().getLocationPermission().replace("channel.", ""))
                        .build());
    }

}
