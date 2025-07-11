package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class LegacyTextComposer implements ChatComposer<String> {

    public static final LegacyTextComposer INSTANCE = new LegacyTextComposer();

    private LegacyTextComposer() {

    }

    @Override
    public String compose(Token token) {
        if (NMSUtil.isPaper()) {
            Component component = ChatComposer.adventure().styles().compose(token);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } else {
            BaseComponent[] components = ChatComposer.styles().compose(token);
            return TextComponent.toLegacyText(components);
        }
    }

    @Override
    public String composeLegacy(String text) {
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

    @Override
    public String composeBungee(BaseComponent[] components) {
        return TextComponent.toLegacyText(components);
    }

    @Override
    public ChatComposer.Adventure<String> composeAdventure() {
        return Adventure.INSTANCE;
    }

    public static final class Adventure implements ChatComposer.Adventure<String> {

        private static final Adventure INSTANCE = new Adventure();
        private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        private Adventure() {

        }

        @Override
        public String compose(Component component) {
            return LEGACY_SERIALIZER.serialize(component);
        }

    }

}
