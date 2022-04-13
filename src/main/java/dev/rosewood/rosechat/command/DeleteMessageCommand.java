package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeleteMessageCommand extends AbstractCommand {

    public DeleteMessageCommand() {
        super(true, "delmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        try {
            UUID uuid = UUID.fromString(args[0]);
            DeletableMessage message = null;

            List<DeletableMessage> toDelete = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());

                for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                    if (!deletableMessage.getUUID().equals(uuid)) continue;

                    RoseSender roseSender = new RoseSender(player);

                    BaseComponent[] deletedMessage = this.parseDeletedMessagePlaceholder(roseSender, roseSender,
                            MessageUtils.getSenderViewerPlaceholders(roseSender, roseSender)
                            .addPlaceholder("id", deletableMessage.getUUID())
                            .addPlaceholder("type", deletableMessage.isClient() ? "client" : "server").build(), deletableMessage);

                    if (deletedMessage == null) {
                        toDelete.add(deletableMessage);
                    } else {
                        if (TextComponent.toPlainText(deletedMessage).isEmpty()) {
                            toDelete.add(deletableMessage);
                        } else {
                            deletableMessage.setJson(ComponentSerializer.toString(deletedMessage));
                        }
                    }

                    message = deletableMessage;
                }

                if (message == null || !message.getUUID().equals(uuid)) continue;
                for (DeletableMessage deletableMessage : toDelete) playerData.getMessageLog().getDeletableMessages().remove(deletableMessage);

                for (int i = 0; i < 100; i++) player.sendMessage("\n");
                for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages())
                    player.spigot().sendMessage(ComponentSerializer.parse(deletableMessage.getJson()));
            }

            // Delete this message from Discord too, if enabled.
            if (!Setting.DELETE_DISCORD_MESSAGES.getBoolean()) return;
            if (message == null) return;
            if (this.getAPI().getDiscord() != null && message.getDiscordId() != null) {
                this.getAPI().getDiscord().deleteMessage(message.getDiscordId());
            }
        } catch (IllegalArgumentException ignored) {}
    }

    private BaseComponent[] parseDeletedMessagePlaceholder(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders, DeletableMessage deletableMessage) {
        String placeholderId = Setting.DELETED_MESSAGE_FORMAT.getString();
        CustomPlaceholder placeholder = this.getAPI().getPlaceholderManager().getPlaceholder(placeholderId.substring(1, placeholderId.length() - 1));
        if (placeholder == null) return null;

        BaseComponent[] component;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        String text = placeholder.getText().parse(sender, viewer, placeholders);
        if (text == null) return null;
        List<Tokenizer<?>> tokenizers = Setting.USE_DISCORD_FORMATTING.getBoolean() ? Tokenizers.DEFAULT_WITH_DISCORD_TOKENIZERS : Tokenizers.DEFAULT_TOKENIZERS;

        text = placeholders.apply(text);
        MessageTokenizer textTokenizer = new MessageTokenizer.Builder()
                .sender(sender).viewer(viewer).location(MessageLocation.OTHER)
                .tokenizers(tokenizers)
                .tokenize(text);
        component = textTokenizer.toComponents();

        String hoverString = placeholder.getHover() != null ? placeholders.apply(placeholder.getHover().parse(sender, viewer, placeholders)) : null;
        if (hoverString != null) {
            if (hoverString.contains("%original%")) {
               hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentSerializer.parse(deletableMessage.getJson()));
            } else {
                MessageTokenizer hoverTokenizer = new MessageTokenizer.Builder()
                        .sender(sender).viewer(viewer).location(MessageLocation.OTHER).tokenizers(tokenizers).tokenize(hoverString);
                BaseComponent[] hover = hoverTokenizer.toComponents();
                hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
            }
        }

        String clickString = placeholder.getClick() != null ? placeholders.apply(placeholder.getClick().parse(sender, viewer, placeholders)) : null;
        ClickEvent.Action action = placeholder.getClick() != null ? placeholder.getClick().parseToAction(sender, viewer, placeholders) : null;
        if (clickString != null && action != null) {
            String click = sender.isPlayer() ? PlaceholderAPIHook.applyPlaceholders(sender.asPlayer(), clickString) : clickString;
            clickEvent = new ClickEvent(action, ChatColor.stripColor(click));
        }

        for (BaseComponent c : component) {
            c.setHoverEvent(hoverEvent);
            c.setClickEvent(clickEvent);
        }

        return component;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }
}
