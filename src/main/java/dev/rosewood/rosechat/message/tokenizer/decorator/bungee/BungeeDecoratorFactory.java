package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorFactory;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.List;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;

public class BungeeDecoratorFactory implements DecoratorFactory {

    public static final DecoratorFactory INSTANCE = new BungeeDecoratorFactory();

    private BungeeDecoratorFactory() {

    }

    @Override
    public ClickDecorator click(ClickDecorator.Action action, String value) {
        return new BungeeClickDecorator(action, value);
    }

    @Override
    public ColorDecorator color(ChatColor chatColor) {
        return new BungeeColorDecorator(chatColor);
    }

    @Override
    public ColorDecorator color(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        return new BungeeColorDecorator(colorGeneratorFunction);
    }

    @Override
    public FontDecorator font(String font) {
        return new BungeeFontDecorator(font);
    }

    @Override
    public FormatDecorator format(FormatDecorator.FormatType formatType, boolean value) {
        return new BungeeFormatDecorator(formatType, value);
    }

    @Override
    public FormatDecorator format(ChatColor chatColor, boolean value) {
        return new BungeeFormatDecorator(chatColor, value);
    }

    @Override
    public HoverDecorator hover(List<String> content) {
        return new BungeeHoverDecorator(content);
    }

    @Override
    public HoverDecorator hover(String content) {
        return new BungeeHoverDecorator(content);
    }

    @Override
    public HoverDecorator hover(ItemStack itemStack, String nbt) {
        return new BungeeHoverDecorator(itemStack, nbt);
    }

}
