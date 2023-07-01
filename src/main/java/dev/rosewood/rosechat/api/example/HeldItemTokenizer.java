package dev.rosewood.rosechat.api.example;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HeldItemTokenizer implements Tokenizer<Token> {

    public final Tokenizer<Token> HELD_ITEM_TOKENIZER;
    private final Map<String, String> saveMethods;
    private Class<?> nmsItemStackClass;
    private Class<?> nbtTagCompoundClass;
    private Method obcItemStackAsNMSCopy;
    private final RoseChatAPI api;

    public HeldItemTokenizer() {
        // Registers this tokenizer before the rosechat tokenizer, as it should have a higher priority.
        HELD_ITEM_TOKENIZER = Tokenizers.registerBefore("rosechat", "held_item", this, Tokenizers.DEFAULT_BUNDLE);
        this.initialiseNMSClasses();

        this.saveMethods = new HashMap<String, String>() {{
           this.put("v1_20_R1", "b");
           this.put("v1_19_R3", "b");
           this.put("v1_18_R2", "b");
           this.put("v1_17_R1", "save");
           this.put("v1_16_R3", "save");
        }};

        this.api = RoseChatAPI.getInstance();
    }

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        // Make sure the input is related to what we are looking for.
        // For example, if the input doesn't start with "[" it definitely isn't a held item tag.
        ChatReplacement replacement = this.api.getReplacementById(Setting.HELD_ITEM_REPLACEMENT.getString());
        if (replacement == null) return null;

        if (!input.startsWith(replacement.getText())) return null;
        if (!roseMessage.getSender().isPlayer()) return null;
        if (!ignorePermissions && !MessageUtils.hasTokenPermission(roseMessage, "rosechat.helditem")) return null;
        if (roseMessage.getSender().asPlayer().getEquipment() == null) return null;

        try {
            // Tokenizer specific code.
            // Gets the json for the item in the player's hand.
            ItemStack item = roseMessage.getSender().asPlayer().getEquipment().getItemInMainHand();
            Object nmsItemStack = this.obcItemStackAsNMSCopy.invoke(null, item);

            Method saveNmsItemStack = nmsItemStackClass.getMethod(this.saveMethods.get(NMSUtil.getVersion()), nbtTagCompoundClass);

            Object nmsNbtTagCompoundObj = nbtTagCompoundClass.newInstance();
            Object itemAsJsonObject = saveNmsItemStack.invoke(nmsItemStack, nmsNbtTagCompoundObj);
            String json = itemAsJsonObject.toString();

            ItemMeta itemMeta = item.getItemMeta();
            String itemName = item.hasItemMeta() && itemMeta.hasDisplayName() ?
                    itemMeta.getDisplayName() : WordUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));

            // Return a token with the settings needed.
            return new Token(new Token.TokenSettings(replacement.getText())
                    .content(replacement.getReplacement())
                    .hover(json)
                    .hoverAction(HoverEvent.Action.SHOW_ITEM)
                    .placeholder("item_name", itemName)
                    .placeholder("item", json)
                    .ignoreTokenizer(this)
                    .ignoreTokenizer(Tokenizers.TAG).ignoreTokenizer(Tokenizers.REGEX_REPLACEMENT)
                    .ignoreTokenizer(Tokenizers.REPLACEMENT)
                    .noCaching());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialiseNMSClasses() {
        try {
            Class<?> obcItemStackClass;
            switch (NMSUtil.getVersion()) {
                case "v1_20_R1":
                case "v1_19_R3":
                case "v1_18_R2":
                case "v1_17_R1":
                    obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + NMSUtil.getVersion() + ".inventory.CraftItemStack");
                    this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
                    this.nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
                    this.nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
                    return;
                case "v1_16_R3":
                    obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + NMSUtil.getVersion() + ".inventory.CraftItemStack");
                    this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
                    this.nmsItemStackClass = Class.forName("net.minecraft.server." + NMSUtil.getVersion() + ".ItemStack");
                    this.nbtTagCompoundClass = Class.forName("net.minecraft.server." + NMSUtil.getVersion() + ".NBTTagCompound");
                    return;
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
