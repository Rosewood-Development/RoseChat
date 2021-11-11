package dev.rosewood.rosechat.message.wrapper.tokenizer.replacement;

import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ReplacementToken extends Token {

    private final ChatReplacement replacement;

    public ReplacementToken(RoseSender sender, RoseSender viewer, ChatReplacement replacement, String originalContent) {
        super(sender, viewer, originalContent);
        this.replacement = replacement;
    }

    @Override
    public BaseComponent[] toComponents() {
        String replacement = this.replacement.getReplacement();
        replacement = replacement.replace("%message%", this.getOriginalContent());
        if (this.replacement.isRegex()) replacement = HexUtils.colorify(replacement);

        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (char c : replacement.toCharArray()) {
            componentBuilder.append(this.replacement.isRegex() ? c + "" : HexUtils.colorify(c + "&f")).font(this.replacement.getFont());
            if (this.replacement.getHoverText() != null)
                componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(HexUtils.colorify(this.replacement.getHoverText()))));
        }

        return componentBuilder.create();
    }

    public String getFont() {
        return this.replacement.getFont();
    }
}
