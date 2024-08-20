package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.nms.NMSAdapter;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeldItemTokenizer extends Tokenizer {

    public static Tokenizer HELD_ITEM_TOKENIZER;

    private final RoseChatAPI api;

    public HeldItemTokenizer() {
        super("held_item");

        this.api = RoseChatAPI.getInstance();

        try {
            NMSAdapter.getHandler().initialiseMethods();
        } catch (Exception e) {
            e.printStackTrace();
            LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eNo NMS save method was found for " + Bukkit.getServer().getBukkitVersion() + ". [item] has been disabled.");
            return;
        }

        // Example of how to register tokenizers
        if (HELD_ITEM_TOKENIZER == null) {
            HELD_ITEM_TOKENIZER = this;
            Tokenizers.DEFAULT_BUNDLE.registerBefore(Tokenizers.ROSECHAT_PLACEHOLDER, this);
            Tokenizers.DEFAULT_DISCORD_BUNDLE.registerBefore(Tokenizers.ROSECHAT_PLACEHOLDER, this);
        } else {
            throw new IllegalStateException("Cannot instantiate more than one HeldItemTokenizer");
        }
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        Replacement replacement = this.api.getReplacementById(Settings.HELD_ITEM_REPLACEMENT.get());
        if (replacement == null)
            return null;

        String input = params.getInput();
        if (!input.startsWith(replacement.getInput().getText()))
            return null;

        if (!params.getSender().isPlayer()
                || !this.hasTokenPermission(params, "rosechat.helditem")
                || params.getSender().asPlayer().getEquipment() == null)
            return new TokenizerResult(Token.text(input), input.length());

        try {
            ItemStack item = params.getSender().asPlayer().getEquipment().getItemInMainHand();

            String json = NMSAdapter.getHandler().getItemStackAsString(params.getSender().asPlayer(), item);
            int amount = item.getAmount();

            ItemMeta itemMeta = item.getItemMeta();
            String itemName = item.hasItemMeta() && itemMeta.hasDisplayName()
                    ? itemMeta.getDisplayName() 
                    : WordUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));

            return new TokenizerResult(Token.group(replacement.getOutput().getText())
                    .decorate(HoverDecorator.of(HoverEvent.Action.SHOW_ITEM, json))
                    .placeholder("item_name", itemName)
                    .placeholder("item", json)
                    .placeholder("amount", amount)
                    .ignoreTokenizer(this)
                    .ignoreTokenizer(Tokenizers.REPLACEMENT)
                    .encapsulate()
                    .build(), replacement.getInput().getText().length());

        } catch (Exception e) {
            return null;
        }
    }

}
