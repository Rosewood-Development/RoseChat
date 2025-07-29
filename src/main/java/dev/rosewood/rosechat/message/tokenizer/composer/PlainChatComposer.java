package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class PlainChatComposer implements ChatComposer<String> {

    public static final PlainChatComposer INSTANCE = new PlainChatComposer();

    private PlainChatComposer() {

    }

    @Override
    public String compose(Token token) {
        StringBuilder builder = new StringBuilder();
        this.compose(token, builder);
        return builder.toString();
    }

    private void compose(Token token, StringBuilder stringBuilder) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        for (Token child : token.getChildren()) {
            switch (child.getType()) {
                case TEXT -> stringBuilder.append(child.getContent());
                case GROUP -> this.compose(child, stringBuilder);
            }
        }
    }

    @Override
    public String composeLegacy(String text) {
        return ChatColor.stripColor(text);
    }

    @Override
    public String composeJson(String json) {
        if (NMSUtil.isPaper()) {
            Component component = GsonComponentSerializer.gson().deserialize(json);
            return PlainTextComponentSerializer.plainText().serialize(component);
        } else {
            BaseComponent[] components = MessageUtils.jsonToBungee(json);
            return ChatColor.stripColor(TextComponent.toLegacyText(components));
        }
    }

    @Override
    public String composeBungee(BaseComponent[] components) {
        return TextComponent.toPlainText(components);
    }

    @Override
    public ChatComposer.Adventure<String> composeAdventure() {
        return Adventure.INSTANCE;
    }

    public static final class Adventure implements ChatComposer.Adventure<String> {

        private static final Adventure INSTANCE = new Adventure();

        private Adventure() {

        }

        @Override
        public String compose(Component component) {
            return PlainTextComponentSerializer.plainText().serialize(component);
        }

    }

}
