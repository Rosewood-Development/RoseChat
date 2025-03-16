package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class HoverDecorator extends TokenDecorator {

    private final HoverEvent.Action action;
    private final List<String> content;

    protected HoverDecorator(HoverEvent.Action action, List<String> content) {
        super(DecoratorType.CONTENT);

        this.action = action;
        this.content = content;
    }

    @Override
    public void apply(BaseComponent component, MessageTokenizer tokenizer, Token parent) {
        if (this.content == null || this.content.isEmpty())
            return;

        if (this.action != HoverEvent.Action.SHOW_TEXT) {
            component.setHoverEvent(new HoverEvent(this.action, new ComponentBuilder(parent.getPlaceholders().apply(this.content.get(0))).create()));
            return;
        }

        // Append all lines together separated by \n and run it all through the tokenizer so colors pass through newlines
        // I can't believe this works
        String combinedContent = String.join("\n", this.content);

        ComponentBuilder componentBuilder = new ComponentBuilder();
        ComponentStyle styleAccumulator = new ComponentStyle();

        Token.Builder builder = Token.group(combinedContent).placeholders(parent.getPlaceholders());
        parent.getIgnoredTokenizers().forEach(builder::ignoreTokenizer);

        Token token = builder.build();
        tokenizer.tokenizeContent(token, 0);
        BaseComponent[] hover = tokenizer.toComponents(token, TokenComposer.styles(tokenizer));
        for (BaseComponent hoverComponent : hover) {
            this.patchAndAccumulateStyles(styleAccumulator, hoverComponent);
            componentBuilder.append(hoverComponent, ComponentBuilder.FormatRetention.NONE);
        }

        component.setHoverEvent(new HoverEvent(this.action, componentBuilder.create()));
    }

    public static HoverDecorator of(HoverEvent.Action action, List<String> content) {
        return new HoverDecorator(Objects.requireNonNullElse(action, HoverEvent.Action.SHOW_TEXT), content);
    }

    public static HoverDecorator of(HoverEvent.Action action, String content) {
        return new HoverDecorator(Objects.requireNonNullElse(action, HoverEvent.Action.SHOW_TEXT), Collections.singletonList(content));
    }

    // Spigot appears to always apply a FormatRetention of ALL across all components in hover
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
