package dev.rosewood.rosechat.message.wrapper.tokenizer.test;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class TestToken extends Token {
    
    public TestToken(String originalContent) {
        super(originalContent);
    }

    @Override
    public String getText(MessageWrapper wrapper) {
        // Whatever this is
        return this.getOriginalContent().substring(1, this.getOriginalContent().length() - 1);
    }

    @Override
    public String getHover(MessageWrapper wrapper) {
        return "ahh";
    }

    @Override
    public HoverEvent.Action getHoverAction() {
        return super.getHoverAction();
    }

    @Override
    public String getClick(MessageWrapper wrapper) {
        return "/test";
    }

    @Override
    public ClickEvent.Action getClickAction() {
        return ClickEvent.Action.SUGGEST_COMMAND;
    }
}
