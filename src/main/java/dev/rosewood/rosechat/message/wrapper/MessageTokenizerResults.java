package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageTokenizerResults {

    private final Token token;
    private final MessageOutputs outputs;
    private final Map<TokenComposer<?>, Object> builtComponentsCache;

    public MessageTokenizerResults(Token token, MessageOutputs outputs) {
        this.token = token;
        this.outputs = outputs;
        this.builtComponentsCache = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T build(TokenComposer<T> composer) {
        return (T) this.builtComponentsCache.computeIfAbsent(composer, x -> x.compose(this.token));
    }

    public BaseComponent[] buildComponents() {
        return this.build(TokenComposer.decorated());
    }

    public void sendMessage(CommandSender receiver) {
        if (NMSUtil.isPaper()) {
            receiver.sendMessage(this.build(TokenComposer.adventure().decorated()));
        } else {
            receiver.sendMessage(this.build(TokenComposer.decorated()));
        }
    }

    public void setDisplayName(Player player) {
        if (NMSUtil.isPaper()) {
            player.displayName(this.build(TokenComposer.adventure().decorated()));
        } else {
            player.setDisplayName(this.build(TokenComposer.legacy()));
        }
    }

    public MessageOutputs outputs() {
        return this.outputs;
    }

    public static MessageTokenizerResults fromLegacy(String text) {
        return new LegacyMessageTokenizerResults(text, new MessageOutputs());
    }

    public static MessageTokenizerResults fromJson(String json) {
        return new JsonMessageTokenizerResults(json, new MessageOutputs());
    }

    private static class LegacyMessageTokenizerResults extends MessageTokenizerResults {

        private final String legacyText;

        public LegacyMessageTokenizerResults(String legacyText, MessageOutputs outputs) {
            super(null, outputs);
            this.legacyText = legacyText;
        }

        @Override
        public <T> T build(TokenComposer<T> composer) {
            return composer.composeLegacyText(this.legacyText);
        }

    }

    private static class JsonMessageTokenizerResults extends MessageTokenizerResults {

        private final String json;

        public JsonMessageTokenizerResults(String json, MessageOutputs outputs) {
            super(null, outputs);
            this.json = json;
        }

        @Override
        public <T> T build(TokenComposer<T> composer) {
            return composer.composeJson(this.json);
        }

    }

}
