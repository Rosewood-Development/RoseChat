package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SidedSignListener extends BaseSignListener {

    private static final NamespacedKey FRONT_LINES_KEY = new NamespacedKey(RoseChat.getInstance(), "front");
    private static final NamespacedKey BACK_LINES_KEY = new NamespacedKey(RoseChat.getInstance(), "back");

    public SidedSignListener(RoseChat plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;

        Block block = event.getClickedBlock();
        if (!Settings.ENABLE_ON_SIGNS.get() || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND || block == null)
            return;

        if (!block.getType().toString().contains("_SIGN"))
            return;

        Player player = event.getPlayer();
        Sign sign = (Sign) block.getState();

    }

    @Override
    public boolean handleInteraction(Block block, Player player, Sign sign, ItemStack item) {
        if (sign.isWaxed())
            return false;

        SignSide side = sign.getTargetSide(player);

        // Open the sign if the player isn't holding an item.
        if (item == null)
            return this.openUnformattedSign(player, sign);

        if (player.isSneaking() && item.getType().isBlock())
            return false;

        Material itemType = item.getType();
        if (NMSUtil.getVersionNumber() >= 17) {
            if ((itemType == Material.GLOW_INK_SAC || itemType == Material.INK_SAC) && player.isSneaking())
                return true;

            if (itemType == Material.GLOW_INK_SAC && !side.isGlowingText()) {
                side.setGlowingText(true);
                sign.update();
                return true;
            } else if (itemType == Material.INK_SAC && side.isGlowingText()) {
                side.setGlowingText(false);
                sign.update();
                return true;
            }
        }

        if (NMSUtil.getVersionNumber() >= 20) {
            if (itemType == Material.HONEYCOMB && !sign.isWaxed()) {
                sign.setWaxed(true);
                sign.update();
                sign.getWorld().spawnParticle(Particle.WAX_ON, sign.getX() + 0.5, sign.getY() + 0.75, sign.getZ() + 0.5, 20, 0.25, 0.25, 0.25, 1);
                player.playSound(sign.getLocation(), Sound.ITEM_HONEYCOMB_WAX_ON, 0.75f, 1.0f);
                return false;
            }
        }

        if (this.dyeColors.containsKey(itemType)) {
            if (player.isSneaking())
                return false;

            DyeColor color = this.dyeColors.get(itemType);
            if (sign.getTargetSide(player).getColor() == color)
                return this.openUnformattedSign(player, sign);

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                Sign updatedSign = (Sign) block.getState();
                SignSide updatedSide = updatedSign.getTargetSide(player);
                SignSide frontSide = updatedSign.getSide(Side.FRONT);
                boolean isFrontSide = (updatedSide == frontSide);
                if (!updatedSign.getPersistentDataContainer().has(isFrontSide ? FRONT_LINES_KEY : BACK_LINES_KEY))
                    return;

                List<String> unformattedLines = updatedSign.getPersistentDataContainer().get(isFrontSide ? FRONT_LINES_KEY : BACK_LINES_KEY,
                        PersistentDataType.LIST.strings());
                if (unformattedLines == null || unformattedLines.isEmpty())
                    return;

                // Grab the unformatted lines from the PDC so the player can see them when editing.
                for (int i = 0; i < unformattedLines.size(); i++)
                    updatedSide.setLine(i, unformattedLines.get(i));

                updatedSign.update();
            }, 0L);

            Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () -> {
                Sign updatedSign = (Sign) block.getState();
                SignSide updatedSide = updatedSign.getTargetSide(player);
                boolean isFrontSide = (updatedSide == updatedSign.getSide(Side.FRONT));
                this.updateSign(player, updatedSign, isFrontSide ? Side.FRONT : Side.BACK, updatedSide.getLines());
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
            this.updateSign(event.getPlayer(), sign, event.getSide(), event.getLines());
        }, 0L);
    }

    private void updateSign(Player player, Sign sign, Side side, String[] lines) {
        SignSide signSide = sign.getSide(side);
        DyeColor signColor = signSide.getColor();
        if (signColor == null)
            return;

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

            components.sidedSigns().setSignText(sign, signSide, i);
        }

        if (!blocked)
            sign.getPersistentDataContainer().set(side == Side.FRONT ? FRONT_LINES_KEY : BACK_LINES_KEY,
                    PersistentDataType.LIST.strings(), Arrays.asList(lines));

        sign.update();
    }

    private boolean openUnformattedSign(Player player, Sign sign) {
        SignSide targetSide = sign.getTargetSide(player);
        SignSide frontSide = sign.getSide(Side.FRONT);
        boolean isFrontSide = (targetSide == frontSide);

        if (!sign.getPersistentDataContainer().has(isFrontSide ? FRONT_LINES_KEY : BACK_LINES_KEY))
            return false;

        List<String> unformattedLines = sign.getPersistentDataContainer().get(isFrontSide ? FRONT_LINES_KEY : BACK_LINES_KEY,
                PersistentDataType.LIST.strings());
        if (unformattedLines == null || unformattedLines.isEmpty())
            return false;

        // Grab the unformatted lines from the PDC so the player can see them when editing.
        for (int i = 0; i < unformattedLines.size(); i++)
            targetSide.setLine(i, unformattedLines.get(i));

        sign.update();

        Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () ->
                player.openSign(sign, isFrontSide ? Side.FRONT : Side.BACK), 2L);
        return true;
    }

}
