package dev.rosewood.rosechat.nms;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class v1_16_R1 implements NMSHandler {

    private Class<?> nbtTagCompoundClass;
    private Method obcItemStackAsNMSCopy;
    private Method saveNMSItemStack;

    @Override
    public void initialiseMethods() throws Exception {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);

        Class<?> obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
        this.nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

        this.saveNMSItemStack = nmsItemStackClass.getMethod("save", this.nbtTagCompoundClass);
    }

    @Override
    public String getItemStackAsString(Player player, ItemStack item) throws Exception {
        Object nmsItemStack = this.obcItemStackAsNMSCopy.invoke(null, item);
        Object nmsNbtTagCompoundObj = this.nbtTagCompoundClass.newInstance();

        Object itemAsJsonObject = this.saveNMSItemStack.invoke(nmsItemStack, nmsNbtTagCompoundObj);

        return itemAsJsonObject.toString();
    }

}
