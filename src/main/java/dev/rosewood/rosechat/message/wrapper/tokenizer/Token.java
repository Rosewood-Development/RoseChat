package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.List;

public abstract class Token {

    protected final String originalContent;
    protected String font;

    public Token(String originalContent) {
        this.originalContent = originalContent;
        this.font = "default";
    }

    public final String getOriginalContent() {
        return this.originalContent;
    }

    public BaseComponent[] toComponents(MessageWrapper wrapper) {
        ComponentBuilder componentBuilder = new ComponentBuilder(this.originalContent);
        return componentBuilder.create();
    }

    public abstract String getText(MessageWrapper wrapper);

    public String getHover(MessageWrapper wrapper) {
        return null;
    }

    public HoverEvent.Action getHoverAction() {
        return HoverEvent.Action.SHOW_TEXT;
    }

    public String getClick(MessageWrapper wrapper) {
        return null;
    }

    public ClickEvent.Action getClickAction() {
        return ClickEvent.Action.RUN_COMMAND;
    }

    public HexUtils.ColorGenerator getColorGenerator(MessageWrapper wrapper, List<Token> futureTokens) {
        return null;
    }

    public boolean hasColorGenerator() {
        return false;
    }

    public String getFont() {
        return this.font;
    }

    public void setFont(String font) {
        this.font = font;
    }

}
