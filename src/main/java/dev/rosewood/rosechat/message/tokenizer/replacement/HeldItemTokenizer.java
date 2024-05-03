package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Method;

public class HeldItemTokenizer extends Tokenizer {

    public static Tokenizer HELD_ITEM_TOKENIZER;

    private Class<?> nbtTagCompoundClass;
    private Class<?> holderLookupClass;
    private Method obcItemStackAsNMSCopy;
    private Method obcPlayerGetHandle;
    private Method saveNmsItemStack;
    private Method registryAccess;
    private final RoseChatAPI api;

    public HeldItemTokenizer() {
        super("held_item");

        // Example of how to register tokenizers
        if (HELD_ITEM_TOKENIZER == null) {
            HELD_ITEM_TOKENIZER = this;
            Tokenizers.DEFAULT_BUNDLE.registerBefore(Tokenizers.ROSECHAT_PLACEHOLDER, this);
            Tokenizers.DEFAULT_DISCORD_BUNDLE.registerBefore(Tokenizers.ROSECHAT_PLACEHOLDER, this);
        } else {
            throw new IllegalStateException("Cannot instantiate more than one HeldItemTokenizer");
        }

        this.initialiseNMSClasses();

        this.api = RoseChatAPI.getInstance();
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        Replacement replacement = this.api.getReplacementById(Setting.HELD_ITEM_REPLACEMENT.getString());
        if (replacement == null) return null;

        String input = params.getInput();
        if (!input.startsWith(replacement.getInput().getText())) return null;

        if (!params.getSender().isPlayer()
                || !MessageUtils.hasTokenPermission(params, "rosechat.helditem")
                || params.getSender().asPlayer().getEquipment() == null) return new TokenizerResult(Token.text(input), input.length());

        try {
            ItemStack item = params.getSender().asPlayer().getEquipment().getItemInMainHand();

            Object nmsItemStack = this.obcItemStackAsNMSCopy.invoke(null, item);
            Object nmsNbtTagCompoundObj = this.nbtTagCompoundClass.newInstance();

            Object itemAsJsonObject;
            if (NMSUtil.getVersionNumber() != 20 || NMSUtil.getMinorVersionNumber() < 5) {
                itemAsJsonObject = this.saveNmsItemStack.invoke(nmsItemStack, nmsNbtTagCompoundObj);
            } else {
                Object nmsPlayer = this.obcPlayerGetHandle.invoke(params.getSender().asPlayer());

                if (this.registryAccess == null)
                    this.registryAccess = nmsPlayer.getClass().getMethod("dR");

                Object registryAccess = this.registryAccess.invoke(nmsPlayer);
                itemAsJsonObject = this.saveNmsItemStack.invoke(nmsItemStack, registryAccess);
            }

            String json = itemAsJsonObject.toString();

            ItemMeta itemMeta = item.getItemMeta();
            String itemName = item.hasItemMeta() && itemMeta.hasDisplayName()
                    ? itemMeta.getDisplayName() 
                    : WordUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));

            return new TokenizerResult(Token.group(replacement.getOutput().getText())
                    .decorate(HoverDecorator.of(HoverEvent.Action.SHOW_ITEM, json))
                    .placeholder("item_name", itemName)
                    .placeholder("item", json)
                    .ignoreTokenizer(this)
                    .ignoreTokenizer(Tokenizers.REPLACEMENT)
                    .encapsulate()
                    .build(), replacement.getInput().getText().length());

        } catch (Exception e) {
            return null;
        }
    }

    private void initialiseNMSClasses() {
        int major = NMSUtil.getVersionNumber();
        int minor = NMSUtil.getMinorVersionNumber();

        String version = null;
        String name = Bukkit.getServer().getClass().getPackage().getName();
        if (name.contains("R")) {
            version = name.substring(name.lastIndexOf('.') + 1);
        }

        try {
            Class<?> obcItemStackClass;
            Class<?> nmsItemStackClass;
            Class<?> obcPlayerClass;
            if (major == 16) {
                obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
                this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
                this.nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

                this.saveNmsItemStack = nmsItemStackClass.getMethod("save", this.nbtTagCompoundClass);
            } else if (version != null) {
                obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
                this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
                this.nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");

                if (major == 20 && minor >= 5) {
                    this.holderLookupClass = Class.forName("net.minecraft.core.HolderLookup$a");
                    obcPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
                    this.obcPlayerGetHandle = obcPlayerClass.getMethod("getHandle");
                    this.saveNmsItemStack = nmsItemStackClass.getMethod("a", this.holderLookupClass);
                    return;
                }

                this.saveNmsItemStack = nmsItemStackClass.getMethod("b", this.nbtTagCompoundClass);
            } else {
                obcItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
                this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
                this.nbtTagCompoundClass = Class.forName("net.minecraft.nbt.CompoundTag");

                this.holderLookupClass = Class.forName("net.minecraft.core.HolderLookup$Provider");
                obcPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
                this.obcPlayerGetHandle = obcPlayerClass.getMethod("getHandle");

                this.saveNmsItemStack = nmsItemStackClass.getMethod("save", this.holderLookupClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eNo NMS save method was found for " + Bukkit.getServer().getBukkitVersion() + ". [item] has been disabled.");
        }
    }

}
