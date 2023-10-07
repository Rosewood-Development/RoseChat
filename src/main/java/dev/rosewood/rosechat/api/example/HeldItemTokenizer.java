package dev.rosewood.rosechat.api.example;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeldItemTokenizer extends Tokenizer {

    public static Tokenizer HELD_ITEM_TOKENIZER;

    private Class<?> nbtTagCompoundClass;
    private Method obcItemStackAsNMSCopy;
    private Method saveNmsItemStack;
    private final RoseChatAPI api;

    public HeldItemTokenizer() {
        super("held_item");

        if (HELD_ITEM_TOKENIZER == null) {
            HELD_ITEM_TOKENIZER = this;
            Tokenizers.DEFAULT_BUNDLE.registerBefore(Tokenizers.ROSECHAT_PLACEHOLDER, this);
        } else {
            throw new IllegalStateException("Cannot instantiate more than one HeldItemTokenizer");
        }

        this.initialiseNMSClasses();

        this.api = RoseChatAPI.getInstance();
    }

    // TODO: Make this work!
    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        Replacement replacement = this.api.getReplacementById(Setting.HELD_ITEM_REPLACEMENT.getString());
        if (replacement == null) return null;

        String input = params.getInput();
        if (!input.startsWith(replacement.getInput().getText())) return null;
        if (!params.getSender().isPlayer()) return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.helditem")) return null;
        if (params.getSender().asPlayer().getEquipment() == null) return null;

        try {
            ItemStack item = params.getSender().asPlayer().getEquipment().getItemInMainHand();
            Object nmsItemStack = this.obcItemStackAsNMSCopy.invoke(null, item);
            Object nmsNbtTagCompoundObj = this.nbtTagCompoundClass.newInstance();
            Object itemAsJsonObject = this.saveNmsItemStack.invoke(nmsItemStack, nmsNbtTagCompoundObj);
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

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialiseNMSClasses() {
        Map<String, String> saveMethods = new HashMap<>() {{
            this.put("v1_20_R2", "b");
            this.put("v1_20_R1", "b");
            this.put("v1_19_R3", "b");
            this.put("v1_18_R2", "b");
            this.put("v1_17_R1", "save");
            this.put("v1_16_R3", "save");
        }};

        try {
            Class<?> obcItemStackClass;
            Class<?> nmsItemStackClass;
            if (NMSUtil.getVersion().equals("v1_16_R3")) {
                obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + NMSUtil.getVersion() + ".inventory.CraftItemStack");
                this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                nmsItemStackClass = Class.forName("net.minecraft.server." + NMSUtil.getVersion() + ".ItemStack");
                this.nbtTagCompoundClass = Class.forName("net.minecraft.server." + NMSUtil.getVersion() + ".NBTTagCompound");
            } else {
                obcItemStackClass = Class.forName("org.bukkit.craftbukkit." + NMSUtil.getVersion() + ".inventory.CraftItemStack");
                this.obcItemStackAsNMSCopy = obcItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
                this.nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
            }
            this.saveNmsItemStack = nmsItemStackClass.getMethod(saveMethods.get(NMSUtil.getVersion()), this.nbtTagCompoundClass);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
