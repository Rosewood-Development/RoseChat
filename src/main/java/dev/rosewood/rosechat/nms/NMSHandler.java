package dev.rosewood.rosechat.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NMSHandler {

    void initialiseMethods() throws Exception;

    String getItemStackAsString(Player player, ItemStack item) throws Exception;

}
