package dev.rosewood.rosechat.message.wrapper.tokenizer.url;

import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class URLToken extends Token {

    private final String content;
    private final String url;
    private final String hover;

    public URLToken(String originalContent, String content, String url, String hover) {
        super(originalContent);

        this.content = content;
        this.url = url;
        this.hover = hover;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public boolean requiresTokenizing() {
        return true;
    }

    @Override
    public String getHover() {
        return hover;
    }

    @Override
    public HoverEvent.Action getHoverAction() {
        return super.getHoverAction();
    }

    @Override
    public String getClick() {
        return this.url;
    }

    @Override
    public ClickEvent.Action getClickAction() {
        return ClickEvent.Action.OPEN_URL;
    }

}
