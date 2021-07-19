package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import java.util.UUID;

/**
 * Temporary message wrapper before new method.
 */
public class MessageWrapper {

    private RoseChat plugin;
    private ComponentBuilder components;
    private MessageSender sender;
    private String prefix;
    private String message;
    private UUID uuid;

    public MessageWrapper(String prefix, MessageSender sender, String message) {
        this.plugin = RoseChat.getInstance();
        this.sender = sender;
        this.prefix = prefix;
        this.message = message;
        this.uuid = UUID.randomUUID();

        this.components = new ComponentBuilder();

        if (sender == null) {
            this.components.append(TextComponent.fromLegacyText(message), ComponentBuilder.FormatRetention.NONE);
        } else {
            String color = sender.isPlayer() ? this.plugin.getManager(DataManager.class).getPlayerData(sender.asPlayer().getUniqueId()).getColor() : "&f";
            String name = !sender.isPlayer() ? sender.getName() : RoseChatAPI.getInstance().getPlayerData(sender.asPlayer().getUniqueId()).getNickname();
            name = name == null ? sender.getName() : HexUtils.colorify(PlaceholderAPIHook.applyPlaceholders(sender.asPlayer(), name));
            this.components.append(TextComponent.fromLegacyText(HexUtils.colorify("[" + prefix + "&f&r] " + "[" + sender.getGroup() + "] " + name + "&f: " + color + message)), ComponentBuilder.FormatRetention.NONE);
        }
    }

    public BaseComponent[] withDelete() {
        BaseComponent[] createdComponents = this.components.create();
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(createdComponents.clone());
        BaseComponent delete = new TextComponent(" ✖");
        delete.setColor(ChatColor.RED);
        delete.setBold(true);
        delete.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to Delete")));
        delete.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delmsg " + this.uuid));
        builder.append(delete);
        String json = ComponentSerializer.toString(createdComponents);
        RoseChatAPI.getInstance().getDataManager().getDeletableMessageLog().add(new DeletableMessage(this.uuid, json, false));
        return builder.create();
    }

    public BaseComponent[] withDeleteSelf() {
        BaseComponent[] createdComponents = this.components.create();
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(createdComponents.clone());
        BaseComponent delete = new TextComponent(" ✖");
        delete.setColor(ChatColor.GREEN);
        delete.setBold(true);
        delete.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to Delete")));
        delete.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delmsg " + this.uuid));
        builder.append(delete);
        String json = ComponentSerializer.toString(createdComponents);
        RoseChatAPI.getInstance().getDataManager().getDeletableMessageLog().add(new DeletableMessage(this.uuid, json, false));
        return builder.create();
    }

    public BaseComponent[] getComponents() {
        BaseComponent[] createdComponents = this.components.create();
        String json = ComponentSerializer.toString(createdComponents);
        RoseChatAPI.getInstance().getDataManager().getDeletableMessageLog().add(new DeletableMessage(this.uuid, json, false));
        return this.components.create();
    }

    public MessageSender getSender() {
        return sender;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getMessage() {
        return this.message;
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
