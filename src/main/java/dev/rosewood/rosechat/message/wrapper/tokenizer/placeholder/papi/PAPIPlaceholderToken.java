package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;

public class PAPIPlaceholderToken extends Token {

    public PAPIPlaceholderToken(String originalContent) {
        super(originalContent);
    }

    @Override
    public String getContent(MessageWrapper wrapper, RoseSender viewer) {
        return this.getOriginalContent().startsWith("%other_") ?
                               PlaceholderAPIHook.applyPlaceholders(viewer.asPlayer(), this.getOriginalContent().replaceFirst("other_", "")) :
                               PlaceholderAPIHook.applyPlaceholders(wrapper.getSender().asPlayer(), this.getOriginalContent());
    }

}
