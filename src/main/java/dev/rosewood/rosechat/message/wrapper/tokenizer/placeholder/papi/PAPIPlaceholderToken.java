package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class PAPIPlaceholderToken extends Token {

    public PAPIPlaceholderToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        String placeholder = PlaceholderAPIHook.applyPlaceholders(this.getSender().asPlayer(), this.getOriginalContent()) + "&f";
        for (char c : placeholder.toCharArray()) {
            componentBuilder.append(String.valueOf(c));
        }

        return componentBuilder.create();
    }
}
