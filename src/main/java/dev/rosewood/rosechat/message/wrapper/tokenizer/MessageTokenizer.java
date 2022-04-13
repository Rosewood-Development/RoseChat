package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import java.util.ArrayList;
import java.util.List;

public class MessageTokenizer {

    private MessageWrapper messageWrapper;
    private Group group;
    private RoseSender sender;
    private RoseSender viewer;
    private MessageLocation location;
    private List<Tokenizer<?>> tokenizers;
    private final List<Token> tokens;
    private boolean colorize;
    private boolean simplify;

    private MessageTokenizer() {
        this.tokens = new ArrayList<>();
        this.tokenizers = Tokenizers.DEFAULT_TOKENIZERS;
        this.colorize = true;
        this.simplify = true;
    }

    private String parseReplacements(String message) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex() || !message.contains(replacement.getText())) continue;
            String groupPermission = this.group == null ? "" : "." + this.group.getLocationPermission();
            if (this.location != MessageLocation.NONE && !this.sender.hasPermission("rosechat.replacements." + this.location.toString().toLowerCase() + groupPermission)
                    || !this.sender.hasPermission("rosechat.replacement." + replacement.getId())) continue;
            message = message.replace(replacement.getText(), replacement.getReplacement());
        }

        return message;
    }

    private void tokenize(String message) {
        this.tokens.clear();
        for (int i = 0; i < message.length(); i++) {
            String substring = message.substring(i);
            for (Tokenizer<?> tokenizer : this.tokenizers) {
                Token token = tokenizer.tokenize(this.messageWrapper, this.group, this.sender, this.viewer, this.location, substring);
                if (token != null) {
                    this.tokens.add(token);
                    i += token.getOriginalContent().length() - 1;
                    break;
                }
            }
        }
    }

    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (Token token : this.tokens) {
            BaseComponent[] components = token.toComponents();
            if (components != null && components.length > 0) componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE).font(token.getFont());
        }

        // Appends an empty string to always have something in the component.
        componentBuilder.append("", ComponentBuilder.FormatRetention.NONE);
        BaseComponent[] components = componentBuilder.create();

        components = this.colorize ? ComponentColorizer.colorize(components) : components;

        return this.simplify ? ComponentSimplifier.simplify(components) : components;
    }

    public static class Builder {

        private final MessageTokenizer tokenizer;

        public Builder() {
            this.tokenizer = new MessageTokenizer();
        }

        public Builder message(MessageWrapper messageWrapper) {
            this.tokenizer.messageWrapper = messageWrapper;
            return this;
        }

        public Builder group(Group group) {
            this.tokenizer.group = group;
            return this;
        }

        public Builder sender(RoseSender sender) {
            this.tokenizer.sender = sender;
            return this;
        }

        public Builder viewer(RoseSender viewer) {
            this.tokenizer.viewer = viewer;
            return this;
        }

        public Builder location(MessageLocation location) {
            this.tokenizer.location = location;
            return this;
        }

        public Builder tokenizers(List<Tokenizer<?>> tokenizers) {
            this.tokenizer.tokenizers = tokenizers;
            return this;
        }

        public Builder colorize(boolean colorize) {
            this.tokenizer.colorize = colorize;
            return this;
        }

        public Builder simplify(boolean simplify) {
            this.tokenizer.simplify = simplify;
            return this;
        }

        public MessageTokenizer tokenize(String message) {
            this.tokenizer.tokenize(this.tokenizer.parseReplacements(message));
            return this.tokenizer;
        }
    }
}
