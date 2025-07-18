package dev.rosewood.rosechat.message.tokenizer.composer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import java.awt.Color;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.inventory.ItemStack;

public class BungeeHoverDecorator extends BungeeTokenDecorator<HoverDecorator> {

    public BungeeHoverDecorator(HoverDecorator decorator) {
        super(decorator);
    }

    @Override
    public void apply(BaseComponent component, Token parent) {
        switch (this.decorator.action()) {
            case SHOW_TEXT -> {
                Token token = this.decorator.getContentToken();
                BaseComponent[] hover = ChatComposer.styles().compose(token);

                ComponentBuilder componentBuilder = new ComponentBuilder();
                ComponentStyle styleAccumulator = new ComponentStyle();
                for (BaseComponent hoverComponent : hover) {
                    this.patchAndAccumulateStyles(styleAccumulator, hoverComponent);
                    componentBuilder.append(hoverComponent, ComponentBuilder.FormatRetention.NONE);
                }

                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, componentBuilder.create()));
            }
            case SHOW_ITEM -> {
                ItemStack item = this.decorator.item();
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(item.getType().getKey().toString(), item.getAmount(), ItemTag.ofNbt(item.getItemMeta().getAsString()))));
            }
        }
    }

    // Bungee appears to always apply a FormatRetention of ALL across all components in hover
    // Why? I don't know, it doesn't appear to happen in vanilla, but we need to forcefully set the component properties
    // to their defaults if they've been previously modified otherwise they get inherited. annoying.
    private void patchAndAccumulateStyles(ComponentStyle styleAccumulator, BaseComponent component) {
        if (styleAccumulator.getColor() != null && component.getColorRaw() == null) component.setColor(ChatColor.WHITE);
        if (styleAccumulator.getFont() != null && component.getFontRaw() == null) component.setFont("minecraft:default");
        if (styleAccumulator.isObfuscatedRaw() != null && component.isObfuscatedRaw() == null) component.setObfuscated(false);
        if (styleAccumulator.isBoldRaw() != null && component.isBoldRaw() == null) component.setBold(false);
        if (styleAccumulator.isItalicRaw() != null && component.isItalicRaw() == null) component.setItalic(false);
        if (styleAccumulator.isUnderlinedRaw() != null && component.isUnderlinedRaw() == null) component.setUnderlined(false);
        if (styleAccumulator.isStrikethroughRaw() != null && component.isStrikethroughRaw() == null) component.setStrikethrough(false);
        if (ShadowColorDecorator.VALID_VERSION && styleAccumulator.getShadowColor() != null && component.getShadowColorRaw() == null) component.setShadowColor(Color.DARK_GRAY); // this is wrong, but I don't really care, should be 75% the brightness of the chat color with 25% the opacity

        if (component.getColorRaw() != null) styleAccumulator.setColor(component.getColorRaw());
        if (component.getFontRaw() != null) styleAccumulator.setFont(component.getFontRaw());
        if (component.isObfuscatedRaw() != null) styleAccumulator.setObfuscated(component.isObfuscatedRaw());
        if (component.isBoldRaw() != null) styleAccumulator.setBold(component.isBoldRaw());
        if (component.isItalicRaw() != null) styleAccumulator.setItalic(component.isItalicRaw());
        if (component.isUnderlinedRaw() != null) styleAccumulator.setUnderlined(component.isUnderlinedRaw());
        if (component.isStrikethroughRaw() != null) styleAccumulator.setStrikethrough(component.isStrikethroughRaw());
        if (ShadowColorDecorator.VALID_VERSION && component.getShadowColorRaw() != null) styleAccumulator.setShadowColor(component.getShadowColor());
    }

}
