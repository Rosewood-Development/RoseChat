package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.Token;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class HoverDecorator implements TokenDecorator {

    private final Action action;
    private final List<String> content;
    private final ItemStack item;
    private Token token;

    public HoverDecorator(Action action, List<String> content, ItemStack item) {
        this.action = action;
        this.content = content;
        this.item = item;
    }

    public HoverDecorator(String content) {
        this(List.of(content));
    }

    public HoverDecorator(List<String> content) {
        this(Action.SHOW_TEXT, content, new ItemStack(Material.AIR, 0));
    }

    public HoverDecorator(ItemStack item, String nbt) {
        this(Action.SHOW_ITEM, List.of(nbt), item);
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.CONTENT;
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        return newDecorator instanceof HoverDecorator;
    }

    @Override
    public Token.Builder getContent() {
        return Token.group(String.join("\n", this.content));
    }

    @Override
    public void setContentToken(Token content) {
        this.token = content;
    }

    public Token getContentToken() {
        return this.token;
    }

    public Action action() {
        return this.action;
    }

    public ItemStack item() {
        return this.item;
    }

    public enum Action {
        SHOW_TEXT,
        SHOW_ITEM
    }

}
