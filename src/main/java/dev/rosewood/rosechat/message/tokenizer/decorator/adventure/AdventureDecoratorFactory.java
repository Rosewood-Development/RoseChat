package dev.rosewood.rosechat.message.tokenizer.decorator.adventure;

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

public class AdventureDecoratorFactory implements DecoratorFactory {

    public static final DecoratorFactory INSTANCE = new AdventureDecoratorFactory();

    private AdventureDecoratorFactory() {

    }

    @Override
    public ClickDecorator click(ClickDecorator.Action action, String value) {
        return new AdventureClickDecorator(action, value);
    }

    @Override
    public ColorDecorator color(ChatColor chatColor) {
        return new AdventureColorDecorator(chatColor);
    }

    @Override
    public ColorDecorator color(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        return new AdventureColorDecorator(colorGeneratorFunction);
    }

    @Override
    public FontDecorator font(String font) {
        return new AdventureFontDecorator(font);
    }

    @Override
    public FormatDecorator format(FormatDecorator.FormatType formatType, boolean value) {
        return new AdventureFormatDecorator(formatType, value);
    }

    @Override
    public FormatDecorator format(ChatColor chatColor, boolean value) {
        return new AdventureFormatDecorator(chatColor, value);
    }

    @Override
    public HoverDecorator hover(List<String> content) {
        return new AdventureHoverDecorator(content);
    }

    @Override
    public HoverDecorator hover(String content) {
        return new AdventureHoverDecorator(content);
    }

    @Override
    public HoverDecorator hover(ItemStack itemStack, String nbt) {
        return new AdventureHoverDecorator(itemStack, nbt);
    }

}
