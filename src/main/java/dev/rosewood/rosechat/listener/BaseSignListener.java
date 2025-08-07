package dev.rosewood.rosechat.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageRules;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public abstract class BaseSignListener implements Listener {

    protected final RoseChat plugin;
    protected final Map<Material, DyeColor> dyeColors;
    protected final boolean usingSignEvent;

    public BaseSignListener(RoseChat plugin) {
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

        if (NMSUtil.isPaper() && (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 1))) {
            Bukkit.getPluginManager().registerEvents(new PlayerOpenSignListener(), plugin);
            this.usingSignEvent = true;
        } else {
            this.usingSignEvent = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (this.usingSignEvent)
            return;

        Block block = event.getClickedBlock();
        if (!Settings.ENABLE_ON_SIGNS.get() || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND || block == null)
            return;

        if (!block.getType().toString().contains("_SIGN"))
            return;

        Player player = event.getPlayer();
        Sign sign = (Sign) block.getState();
        ItemStack item = event.getItem();

        if (this.handleInteraction(block, player, sign, item))
            event.setCancelled(true);
    }

    /**
     * Handles a sign interaction
     * @return true to cancel the base interaction, false otherwise
     */
    public abstract boolean handleInteraction(Block block, Player player, Sign sign, ItemStack item);

    protected MessageContents parseLine(RosePlayer player, String text, PermissionArea area) {
        RoseMessage message = RoseMessage.forLocation(player, area);
        message.setPlayerInput(text);
        message.setUsePlayerChatColor(false);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        MessageRules.RuleOutputs outputs = rules.apply(message, text);

        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null) {
                if (outputs.getWarningMessage() != null) {
                    player.send(outputs.getWarningMessage());
                } else {
                    outputs.getWarning().send(player);
                }
            }

            if (Settings.SEND_BLOCKED_MESSAGES_TO_STAFF.get() && outputs.shouldNotifyStaff()) {
                for (Player staffPlayer : Bukkit.getOnlinePlayers()) {
                    if (staffPlayer.hasPermission("rosechat.seeblocked")) {
                        RosePlayer rosePlayer = new RosePlayer(staffPlayer);
                        rosePlayer.sendLocaleMessage("blocked-message",
                                StringPlaceholders.of("player", message.getSender().getName(),
                                        "message", text));
                    }
                }
            }

            return null;
        }

        message.setPlayerInput(outputs.getFilteredMessage());
        return message.parse(player, "{message}");
    }

    private class PlayerOpenSignListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPlayerOpenSign(PlayerOpenSignEvent event) {
            Sign sign = event.getSign();
            Block block = sign.getBlock();
            Player player = event.getPlayer();
            if (event.getCause() != PlayerOpenSignEvent.Cause.PLUGIN && BaseSignListener.this.handleInteraction(block, player, sign, null))
                event.setCancelled(true);
        }

    }

}
