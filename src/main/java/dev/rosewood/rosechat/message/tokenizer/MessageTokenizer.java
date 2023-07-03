package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public interface MessageTokenizer {

    BaseComponent[] toComponents();

    static MessageTokenizer from(RoseMessage roseMessage, RosePlayer viewer, String message, boolean ignorePermissions, String... tokenizerBundles) {
        if (viewer.isPlayer()) {
            return new PlayerMessageTokenizer(roseMessage, viewer, message, ignorePermissions, tokenizerBundles);
        } else {
            // TODO: Implement console message tokenizer which ignores hovers and clicks
            return new PlayerMessageTokenizer(roseMessage, viewer, message, ignorePermissions, tokenizerBundles);
        }
    }

}
