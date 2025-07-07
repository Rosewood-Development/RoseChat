package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class PlainTokenComposer implements TokenComposer<String> {

    public static final PlainTokenComposer INSTANCE = new PlainTokenComposer();

    private PlainTokenComposer() {

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
    public String composeLegacyText(String text) {
        return ChatColor.stripColor(text);
    }

    @Override
    public String composeJson(String json) {
        if (NMSUtil.isPaper()) {
            Component component = GsonComponentSerializer.gson().deserialize(json);
            return PlainTextComponentSerializer.plainText().serialize(component);
        } else {
            BaseComponent[] components = ComponentSerializer.parse(json);
            return ChatColor.stripColor(TextComponent.toLegacyText(components));
        }
    }

}
