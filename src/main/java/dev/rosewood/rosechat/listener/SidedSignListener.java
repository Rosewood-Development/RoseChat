package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidedSignListener implements Listener {

    public static final NamespacedKey FRONT_LINES_KEY = new NamespacedKey(RoseChat.getInstance(), "front");
    public static final NamespacedKey BACK_LINES_KEY = new NamespacedKey(RoseChat.getInstance(), "back");

    private final RoseChat plugin;
    private final Map<Material, DyeColor> dyeColors;

    public SidedSignListener(RoseChat plugin) {
        this.plugin = plugin;
        this.dyeColors = new HashMap<>(){{
           this.put(Material.WHITE_DYE, DyeColor.WHITE);
           this.put(Material.ORANGE_DYE, DyeColor.ORANGE);
           this.put(Material.MAGENTA_DYE, DyeColor.MAGENTA);
           this.put(Material.LIGHT_BLUE_DYE, DyeColor.LIGHT_BLUE);
           this.put(Material.YELLOW_DYE, DyeColor.YELLOW);
           this.put(Material.LIME_DYE, DyeColor.LIME);
           this.put(Material.PINK_DYE, DyeColor.PINK);
           this.put(Material.GRAY_DYE, DyeColor.GRAY);
           this.put(Material.LIGHT_GRAY_DYE, DyeColor.LIGHT_GRAY);
           this.put(Material.CYAN_DYE, DyeColor.CYAN);
           this.put(Material.PURPLE_DYE, DyeColor.PURPLE);
           this.put(Material.BLUE_DYE, DyeColor.BLUE);
           this.put(Material.BROWN_DYE, DyeColor.BROWN);
           this.put(Material.GREEN_DYE, DyeColor.GREEN);
           this.put(Material.RED_DYE, DyeColor.RED);
           this.put(Material.BLACK_DYE, DyeColor.BLACK);
        }};
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Settings.ENABLE_ON_SIGNS.get() || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (!event.getClickedBlock().getType().toString().contains("_SIGN"))
            return;

        Player player = event.getPlayer();
        Sign sign = (Sign) event.getClickedBlock().getState();
        SignSide side = sign.getTargetSide(player);

        // Open the sign if the player isn't holding an item.
        if (event.getItem() == null) {
            event.setCancelled(openUnformattedSign(player, sign));
            return;
        }

        if (player.isSneaking() && event.getItem().getType().isBlock())
            return;

        if (NMSUtil.getVersionNumber() >= 17) {
            if (event.getItem().getType() == Material.GLOW_INK_SAC || event.getItem().getType() == Material.INK_SAC) {
                if (player.isSneaking()) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getItem().getType() == Material.GLOW_INK_SAC && !side.isGlowingText()) {
                side.setGlowingText(true);
                sign.update();
                event.setCancelled(true);
                return;
            } else if (event.getItem().getType() == Material.INK_SAC && side.isGlowingText()) {
                side.setGlowingText(false);
                sign.update();
                event.setCancelled(true);
                return;
            }
        }

        if (this.dyeColors.containsKey(event.getItem().getType())) {
            if (player.isSneaking())
                return;

            DyeColor color = this.dyeColors.get(event.getItem().getType());
            if (sign.getTargetSide(player).getColor() == color) {
                event.setCancelled(openUnformattedSign(player, sign));
                return;
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                Sign updatedSign = (Sign) event.getClickedBlock().getState();
                SignSide updatedSide = updatedSign.getTargetSide(event.getPlayer());
                SignSide frontSide = updatedSign.getSide(Side.FRONT);
                boolean isFrontSide = (updatedSide == frontSide);
                if (!updatedSign.getPersistentDataContainer().has(isFrontSide ? FRONT_LINES_KEY : BACK_LINES_KEY))
                    return;

                List<String> unformattedLines = updatedSign.getPersistentDataContainer().get(isFrontSide ? FRONT_LINES_KEY : BACK_LINES_KEY,
                        PersistentDataType.LIST.strings());
                if (unformattedLines == null || unformattedLines.isEmpty())
                    return;

                event.setCancelled(true);

                // Grab the unformatted lines from the PDC so the player can see them when editing.
                for (int i = 0; i < unformattedLines.size(); i++)
                    updatedSide.setLine(i, unformattedLines.get(i));

                updatedSign.update();
            }, 0L);

            Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () -> {
                Sign updatedSign = (Sign) event.getClickedBlock().getState();
                SignSide updatedSide = updatedSign.getTargetSide(player);
                this.updateSign(player, updatedSign, updatedSide.getLines());
            }, 0L);

            return;
        }

        event.setCancelled(openUnformattedSign(player, sign));
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!Settings.ENABLE_ON_SIGNS.get())
            return;

        // Store the unformatted lines in PDC to edit later.
        Sign sign = (Sign) event.getBlock().getState();
        sign.getPersistentDataContainer().set(event.getSide() == Side.FRONT ? FRONT_LINES_KEY : BACK_LINES_KEY,
                PersistentDataType.LIST.strings(), Arrays.asList(event.getLines()));

        Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () -> {
            this.updateSign(event.getPlayer(), sign, event.getLines());
        }, 0L);
    }

    private void updateSign(Player player, Sign sign, String[] lines) {
        SignSide side = sign.getTargetSide(player);
        DyeColor signColor = side.getColor();
        if (signColor == null)
            return;

        Color color = signColor.getColor();
        String hexColor = String.format("#%02X%02X%02X", (int) (color.getRed() / (sign.getLightLevel() * 0.1)),
                (int) (color.getGreen() / (sign.getLightLevel() * 0.1)), (int) (color.getBlue() / (sign.getLightLevel() * 0.1)));

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty())
                continue;

            MessageTokenizerResults<BaseComponent[]> components = this.parseLine(new RosePlayer(player), line, PermissionArea.SIGN);
            if (components == null || components.content().length == 0)
                continue;

            ComponentBuilder builder = new ComponentBuilder();
            for (BaseComponent component : components.content()) {
                builder.append(component);
                if (component.getColorRaw() == null)
                    builder.color(ChatColor.of(hexColor));
            }

            side.setLine(i, TextComponent.toLegacyText(builder.build()));
        }

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

    private MessageTokenizerResults<BaseComponent[]>  parseLine(RosePlayer player, String text, PermissionArea area) {
        RoseMessage message = RoseMessage.forLocation(player, area);
        message.setPlayerInput(text);
        message.setUsePlayerChatColor(false);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        MessageRules.RuleOutputs outputs = rules.apply(message, text);

        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(player);

            for (Player staffPlayer : Bukkit.getOnlinePlayers()) {
                if (staffPlayer.hasPermission("rosechat.seeblocked")) {
                    RosePlayer rosePlayer = new RosePlayer(staffPlayer);
                    rosePlayer.sendLocaleMessage("blocked-message",
                            StringPlaceholders.of("player", message.getSender().getName(),
                                    "message", text));
                }
            }

            return null;
        }

        message.setPlayerInput(outputs.getFilteredMessage());
        return message.parse(player, "{message}");
    }

}
