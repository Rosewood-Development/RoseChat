package dev.rosewood.rosechat.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class v1_21_R3 implements NMSHandler {

    private Method obcItemStackAsNMSCopy;
    private Method obcPlayerGetHandle;
    private Method saveNMSItemStack;
    private Method registryAccess;

    @Override
    public void initialiseMethods() throws Exception {
        if (NMSAdapter.isPaper()) {
            Class<?> obcItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");

            Class<?> holderLookupClass = Class.forName("net.minecraft.core.HolderLookup$Provider");
            Class<?> obcPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            this.obcPlayerGetHandle = obcPlayerClass.getMethod("getHandle");
            this.saveNMSItemStack = nmsItemStackClass.getMethod("save", holderLookupClass);
        } else {
            Class<?> obcItemStackClass = Class.forName("org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack");
            this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");

            Class<?> holderLookupClass = Class.forName("net.minecraft.core.HolderLookup$a");
            Class<?> obcPlayerClass = Class.forName("org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer");
            this.obcPlayerGetHandle = obcPlayerClass.getMethod("getHandle");
            this.saveNMSItemStack = nmsItemStackClass.getMethod("a", holderLookupClass);
        }
    }

    @Override
    public String getItemStackAsString(Player player, ItemStack item) throws Exception {
        Object nmsItemStack = this.obcItemStackAsNMSCopy.invoke(null, item);

        Object nmsPlayer = this.obcPlayerGetHandle.invoke(player);
        if (this.registryAccess == null)
            this.registryAccess = nmsPlayer.getClass().getMethod("dX");

        Object registryAccess = this.registryAccess.invoke(nmsPlayer);
        Object itemAsJsonObject = this.saveNMSItemStack.invoke(nmsItemStack, registryAccess);

        return itemAsJsonObject.toString();
    }

}
