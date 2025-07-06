package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.decorator.adventure.AdventureDecoratorFactory;
import dev.rosewood.rosechat.message.tokenizer.decorator.bungee.BungeeDecoratorFactory;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.util.List;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;

public interface DecoratorFactory {

    ClickDecorator click(ClickDecorator.Action action, String value);

    ColorDecorator color(ChatColor chatColor);

    ColorDecorator color(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction);

    FontDecorator font(String font);

    FormatDecorator format(FormatDecorator.FormatType formatType, boolean value);

    FormatDecorator format(ChatColor chatColor, boolean value);

    HoverDecorator hover(List<String> content);

    HoverDecorator hover(String content);

    HoverDecorator hover(ItemStack itemStack, String nbt);

    static DecoratorFactory bungee() {
        return BungeeDecoratorFactory.INSTANCE;
    }

    static DecoratorFactory adventure() {
        return AdventureDecoratorFactory.INSTANCE;
    }

    static DecoratorFactory any() {
        if (NMSUtil.isPaper()) {
            return AdventureDecoratorFactory.INSTANCE;
        } else {
            return BungeeDecoratorFactory.INSTANCE;
        }
    }

}
