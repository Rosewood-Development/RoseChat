package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class JsonComposer implements TokenComposer<String> {

    public static final JsonComposer INSTANCE = new JsonComposer();

    private JsonComposer() {

    }

    @Override
    public String compose(Token token) {
        if (NMSUtil.isPaper()) {
            Component component = TokenComposer.adventure().decorated().compose(token);
            return GsonComponentSerializer.gson().serialize(component);
        } else {
            BaseComponent[] components = TokenComposer.decorated().compose(token);
            return ComponentSerializer.toString(components);
        }
    }

    @Override
    public String composeLegacyText(String text) {
        if (NMSUtil.isPaper()) {
            Component component = LegacyComponentSerializer.legacySection().deserialize(text);
            return GsonComponentSerializer.gson().serialize(component);
        } else {
            BaseComponent[] components = TextComponent.fromLegacyText(text);
            return ComponentSerializer.toString(components);
        }
    }

    @Override
    public String composeJson(String json) {
        return json;
    }

}
