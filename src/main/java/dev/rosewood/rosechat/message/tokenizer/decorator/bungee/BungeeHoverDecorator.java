package dev.rosewood.rosechat.message.tokenizer.decorator.bungee;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.inventory.ItemStack;

public class BungeeHoverDecorator extends HoverDecorator implements BungeeTokenDecorator {

    public BungeeHoverDecorator(List<String> content) {
        super(content);
    }

    public BungeeHoverDecorator(String content) {
        super(List.of(content));
    }

    public BungeeHoverDecorator(ItemStack itemStack, String nbt) {
        super(itemStack, nbt);
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        switch (this.action) {
            case SHOW_TEXT -> {
                if (this.content == null || this.content.isEmpty())
                    return;

                // Append all lines together separated by \n and run it all through the tokenizer so colors pass through newlines
                // I can't believe this works
                String combinedContent = String.join("\n", this.content);

                ComponentBuilder componentBuilder = new ComponentBuilder();
                ComponentStyle styleAccumulator = new ComponentStyle();

                Token.Builder builder = Token.group(combinedContent).placeholders(parent.getPlaceholders());
                parent.getIgnoredTokenizers().forEach(builder::ignoreTokenizer);

                Token token = builder.build();
                tokenizer.tokenize(token, tokenizer.getLastDecoratorFactory());
                BaseComponent[] hover = tokenizer.compose(token, TokenComposer.styles(tokenizer));
                for (BaseComponent hoverComponent : hover) {
                    this.patchAndAccumulateStyles(styleAccumulator, hoverComponent);
                    componentBuilder.append(hoverComponent, ComponentBuilder.FormatRetention.NONE);
                }

                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, componentBuilder.create()));
            }
            case SHOW_ITEM -> component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(this.item.getType().getKey().toString(), this.item.getAmount(), ItemTag.ofNbt(this.content.getFirst()))));
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

        if (component.getColorRaw() != null) styleAccumulator.setColor(component.getColorRaw());
        if (component.getFontRaw() != null) styleAccumulator.setFont(component.getFontRaw());
        if (component.isObfuscatedRaw() != null) styleAccumulator.setObfuscated(component.isObfuscatedRaw());
        if (component.isBoldRaw() != null) styleAccumulator.setBold(component.isBoldRaw());
        if (component.isItalicRaw() != null) styleAccumulator.setItalic(component.isItalicRaw());
        if (component.isUnderlinedRaw() != null) styleAccumulator.setUnderlined(component.isUnderlinedRaw());
        if (component.isStrikethroughRaw() != null) styleAccumulator.setStrikethrough(component.isStrikethroughRaw());
    }

}
