package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosegarden.utils.NMSUtil;
import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeldItemTokenizer extends Tokenizer {

    public static Tokenizer HELD_ITEM_TOKENIZER;

    private final RoseChatAPI api;

    public HeldItemTokenizer() {
        super("held_item");

        this.api = RoseChatAPI.getInstance();

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
        Filter filter = this.api.getFilterById(Settings.HELD_ITEM_REPLACEMENT.get());
        if (filter == null)
            return null;

        String input = params.getInput();
        for (String match : filter.matches()) {
            if (!input.startsWith(match))
                return null;

            if (!params.getSender().isPlayer()
                    || !this.hasTokenPermission(params, "rosechat.helditem")
                    || params.getSender().asPlayer().getEquipment() == null)
                return new TokenizerResult(Token.text(input), input.length());

            try {
                ItemStack item = params.getSender().asPlayer().getEquipment().getItemInMainHand();

                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta == null)
                    return new TokenizerResult(Token.text(input), input.length());

                int amount = item.getAmount();
                String json = itemMeta.getAsString();

                String itemName;
                if (itemMeta.hasDisplayName()) {
                    itemName = itemMeta.getDisplayName();
                } else if (NMSUtil.getVersionNumber() >= 21 && itemMeta.hasItemName()) {
                    itemName = itemMeta.getItemName();
                } else {
                    itemName = WordUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));
                }

                return new TokenizerResult(Token.group(filter.replacement())
                        .decorate(params.decorators().hover(item, json))
                        .placeholder("item_name", itemName)
                        .placeholder("item", json)
                        .placeholder("amount", amount)
                        .ignoreTokenizer(this)
                        .ignoreTokenizer(Tokenizers.FILTER)
                        .encapsulate()
                        .build(), match.length());
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

}
