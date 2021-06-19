package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Temporary message wrapper before new method.
 */
public class MessageWrapper {

    private RoseChat plugin;
    private BaseComponent[] components;
    private MessageSender sender;

    public MessageWrapper(String prefix, MessageSender sender, String message) {
        this.plugin = RoseChat.getInstance();
        this.sender = sender;

        if (sender == null) {
            components = TextComponent.fromLegacyText(message);
        } else {
            String color = sender.isPlayer() ? this.plugin.getManager(DataManager.class).getPlayerData(sender.asPlayer().getUniqueId()).getColor() : "&f";
            components = TextComponent.fromLegacyText(HexUtils.colorify("[" + prefix + "&f] " + "[" + sender.getGroup() + "] " + sender.getName() + ": " + color + message));
        }
    }

    public BaseComponent[] getComponents() {
        return components;
    }

    public MessageSender getSender() {
        return sender;
    }
}
