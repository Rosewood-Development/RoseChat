package dev.rosewood.rosechat.message.tokenizer.decorator;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class HoverDecorator implements TokenDecorator {

    protected final Action action;
    protected final List<String> content;
    protected final ItemStack item;

    protected HoverDecorator(List<String> content) {
        this.action = Action.SHOW_TEXT;
        this.content = content;
        this.item = new ItemStack(Material.AIR, 0);
    }

    protected HoverDecorator(ItemStack item, String nbt) {
        this.action = Action.SHOW_ITEM;
        this.content = List.of(nbt);
        this.item = item;
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.CONTENT;
    }

    public enum Action {
        SHOW_TEXT,
        SHOW_ITEM
    }

}
