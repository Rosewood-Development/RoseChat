package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class LegacyTextComposer implements TokenComposer<String> {

    public static final LegacyTextComposer INSTANCE = new LegacyTextComposer();

    private LegacyTextComposer() {

    }

    @Override
    public String compose(Token token) {
        if (NMSUtil.isPaper()) {
            Component component = TokenComposer.adventure().styles().compose(token);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } else {
            BaseComponent[] components = TokenComposer.styles().compose(token);
            return TextComponent.toLegacyText(components);
        }
    }

    @Override
    public String composeLegacyText(String text) {
        return text;
    }

    @Override
    public String composeJson(String json) {
        if (NMSUtil.isPaper()) {
            Component component = GsonComponentSerializer.gson().deserialize(json);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } else {
            BaseComponent[] components = ComponentSerializer.parse(json);
            return TextComponent.toLegacyText(components);
        }
    }

}
