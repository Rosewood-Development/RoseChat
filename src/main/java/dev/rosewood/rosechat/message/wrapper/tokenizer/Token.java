package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.RoseSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Token {

    private final RoseSender sender;
    private final RoseSender viewer;
    private final String originalContent;

    public Token(RoseSender sender, RoseSender viewer, String originalContent) {
        this.sender = sender;
        this.viewer = viewer;
        this.originalContent = originalContent;
    }

    public final String getOriginalContent() {
        return this.originalContent;
    }

    public RoseSender getSender() {
        return this.sender;
    }

    public RoseSender getViewer() {
        return this.viewer;
    }

    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder(this.originalContent);
        componentBuilder.font("default");
        return componentBuilder.create();
    }

    public String asString() {
        return this.originalContent;
    }
}
