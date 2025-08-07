package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SignListener extends BaseSignListener {

    private static final NamespacedKey LINES_KEY = new NamespacedKey(RoseChat.getInstance(), "lines");

    public SignListener(RoseChat plugin) {
        super(plugin);
    }

    @Override
    public boolean handleInteraction(Block block, Player player, Sign sign, ItemStack item) {
        // Open the sign if the player isn't holding an item.
        if (item == null)
            return this.openUnformattedSign(player, sign);

        Material itemType = item.getType();
        if (player.isSneaking() && itemType.isBlock())
            return false;

        if (NMSUtil.getVersionNumber() >= 17) {
            if ((itemType == Material.GLOW_INK_SAC || itemType == Material.INK_SAC) && player.isSneaking())
                if (player.isSneaking())
                    return true;

            if (itemType == Material.GLOW_INK_SAC && !sign.isGlowingText()) {
                sign.setGlowingText(true);
                sign.update();
                return true;
            } else if (itemType == Material.INK_SAC && sign.isGlowingText()) {
                sign.setGlowingText(false);
                sign.update();
                return true;
            }
        }

        if (this.dyeColors.containsKey(itemType)) {
            if (player.isSneaking())
                return false;

            DyeColor color = this.dyeColors.get(itemType);
            if (sign.getColor() == color)
                return this.openUnformattedSign(player, sign);

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                Sign updatedSign = (Sign) block.getState();
                if (!updatedSign.getPersistentDataContainer().has(LINES_KEY))
                    return;

                String unformattedLinesStr = sign.getPersistentDataContainer().get(LINES_KEY,
                        PersistentDataType.STRING);
                if (unformattedLinesStr == null)
                    return;

                String[] unformattedLines = unformattedLinesStr.split("\n");
                if (unformattedLines.length == 0)
                    return;

                // Grab the unformatted lines from the PDC so the player can see them when editing.
                for (int i = 0; i < unformattedLines.length; i++)
                    sign.setLine(i, unformattedLines[i]);

                updatedSign.update();
            }, 0L);

            Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () -> {
                Sign updatedSign = (Sign) block.getState();
                this.updateSign(player, updatedSign, updatedSign.getLines());
            }, 0L);

            return false;
        }

        return this.openUnformattedSign(player, sign);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!Settings.ENABLE_ON_SIGNS.get())
            return;

        // Store the unformatted lines in PDC to edit later.
        Sign sign = (Sign) event.getBlock().getState();
        Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () -> {
            this.updateSign(event.getPlayer(), sign, event.getLines());
        }, 0L);
    }

    private void updateSign(Player player, Sign sign, String[] lines) {
        boolean blocked = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty())
                continue;

            MessageContents components = this.parseLine(new RosePlayer(player), line, PermissionArea.SIGN);
            if (components == null) {
                blocked = true;
                continue;
            }

            String plainLine = components.build(ChatComposer.plain());
            if (plainLine.isBlank())
                continue;

            components.setSignText(sign, i);
        }

        if (!blocked)
            sign.getPersistentDataContainer().set(LINES_KEY,
                    PersistentDataType.STRING, StringUtils.join(sign.getLines(), "\n"));

        sign.update();
    }

    private boolean openUnformattedSign(Player player, Sign sign) {
        if (!sign.getPersistentDataContainer().has(LINES_KEY))
            return false;

        String unformattedLinesStr = sign.getPersistentDataContainer().get(LINES_KEY,
                PersistentDataType.STRING);
        if (unformattedLinesStr == null)
            return false;

        String[] unformattedLines = unformattedLinesStr.split("\n");
        if (unformattedLines.length == 0)
            return false;

        // Grab the unformatted lines from the PDC so the player can see them when editing.
        for (int i = 0; i < unformattedLines.length; i++)
            sign.setLine(i, unformattedLines[i]);

        sign.update();

        Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () ->
                player.openSign(sign), 2L);
        return true;
    }

}
