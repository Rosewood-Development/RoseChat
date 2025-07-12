package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class JsonComposer implements ChatComposer<String> {

    public static final JsonComposer INSTANCE = new JsonComposer();

    private JsonComposer() {

    }

    @Override
    public String compose(Token token) {
        if (NMSUtil.isPaper()) {
            Component component = ChatComposer.adventure().decorated().compose(token);
            return GsonComponentSerializer.gson().serialize(component);
        } else {
            BaseComponent[] components = ChatComposer.decorated().compose(token);
            return MessageUtils.bungeeToJson(components);
        }
    }

    @Override
    public String composeLegacy(String text) {
        if (NMSUtil.isPaper()) {
            Component component = LegacyComponentSerializer.legacySection().deserialize(text);
            return GsonComponentSerializer.gson().serialize(component);
        } else {
            BaseComponent[] components = TextComponent.fromLegacyText(text);
            return MessageUtils.bungeeToJson(components);
        }
    }

    @Override
    public String composeJson(String json) {
        return json;
    }

    @Override
    public String composeBungee(BaseComponent[] components) {
        return MessageUtils.bungeeToJson(components);
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
            return GsonComponentSerializer.gson().serialize(component);
        }

    }

}
