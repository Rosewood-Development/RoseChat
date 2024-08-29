package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!Settings.ENABLE_ON_SIGNS.get())
            return;

        RosePlayer player = new RosePlayer(event.getPlayer());

        for (int i = 0; i < event.getLines().length; i++) {
            String line = event.getLine(i);
            if (line == null || line.isEmpty())
                continue;

            MessageTokenizerResults<BaseComponent[]> components = this.parseLine(player, ChatColor.BLACK + line, PermissionArea.SIGN);
            if (components == null) {
                event.setCancelled(true);
                return;
            }

            event.setLine(i, TextComponent.toLegacyText(components.content()));
        }
    }

    private MessageTokenizerResults<BaseComponent[]>  parseLine(RosePlayer player, String text, PermissionArea area) {
        RoseMessage message = RoseMessage.forLocation(player, area);
        message.setPlayerInput(text);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        MessageRules.RuleOutputs outputs = rules.apply(message, text);

        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(player);
            return null;
        }

        message.setPlayerInput(outputs.getFilteredMessage());
        return message.parse(player, "{message}");
    }

}
