package dev.rosewood.rosechat.message.contents;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents a parsed message and its outputs.
 */
public abstract class MessageContents {

    private final MessageOutputs outputs;
    private final Map<ChatComposer<?>, Object> builtComponentsCache;

    MessageContents(MessageOutputs outputs) {
        this.outputs = outputs;
        this.builtComponentsCache = new HashMap<>();
    }

    protected abstract <T> T compose(ChatComposer<T> composer);

    /**
     * Builds this message as represented by the given {@link ChatComposer}.
     *
     * @param composer The composer to build this message with
     * @return The build message
     * @param <T> The type to build using this message
     */
    @SuppressWarnings("unchecked")
    public <T> T build(ChatComposer<T> composer) {
        return (T) this.builtComponentsCache.computeIfAbsent(composer, this::compose);
    }

    /**
     * @return this message as a bungee components array
     */
    public BaseComponent[] buildComponents() { // Anything using this directly should probably be using something else instead to be more platform-independent
        return this.build(ChatComposer.decorated());
    }

    public MessageOutputs outputs() {
        return this.outputs;
    }

    // region Converter methods

    /**
     * Returns a new MessageContents with the contents set to a Token
     *
     * @param token The token content
     * @return A new MessageContents with the same outputs but new content
     */
    public MessageContents withTokenContents(Token token) {
        return MessageContents.fromToken(token, this.outputs);
    }

    /**
     * Returns a new MessageContents with the contents set to a legacy string
     *
     * @param text The legacy text content
     * @return A new MessageContents with the same outputs but new content
     */
    public MessageContents withLegacyContents(String text) {
        return MessageContents.fromLegacy(text, this.outputs);
    }

    /**
     * Returns a new MessageContents with the contents set to a json string
     *
     * @param json The json content
     * @return A new MessageContents with the same outputs but new content
     */
    public MessageContents withJsonContents(String json) {
        return MessageContents.fromJson(json, this.outputs);
    }

    /**
     * Returns a new MessageContents with the contents set to bungee components
     *
     * @param components The bungee components content
     * @return A new MessageContents with the same outputs but new content
     */
    public MessageContents withBungeeContents(BaseComponent[] components) {
        return MessageContents.fromBungee(components, this.outputs);
    }

    /**
     * @return the separate Adventure MessageContents instance to avoid classloader issues when running on Spigot
     */
    public AdventureContents withAdventure() {
        return new AdventureContents();
    }

    /**
     * The separate Adventure MessageContents instance to avoid classloader issues when running on Spigot
     */
    public final class AdventureContents {

        private AdventureContents() {

        }

        /**
         * Returns a new MessageContents with the contents set to an Adventure component
         *
         * @param component The Adventure component content
         * @return A new MessageContents with the same outputs but new content
         */
        public MessageContents contents(Component component) {
            return MessageContents.adventure().fromAdventure(component, MessageContents.this.outputs);
        }

    }

    // endregion

    // region Platform-independent helper methods

    public void sendMessage(CommandSender receiver) {
        if (NMSUtil.isPaper()) {
            receiver.sendMessage(this.build(ChatComposer.adventure().decorated()));
        } else {
            receiver.sendMessage(this.build(ChatComposer.decorated()));
        }
    }

    public void setDisplayName(Player player) {
        if (NMSUtil.isPaper()) {
            player.displayName(this.build(ChatComposer.adventure().decorated()));
        } else {
            player.setDisplayName(this.build(ChatComposer.legacy()));
        }
    }

    public void setSignText(Sign sign, int lineIndex) {
        DyeColor signColor = sign.getColor();
        Color color = signColor != null ? signColor.getColor() : null;
        String hexColor = color != null ? String.format("#%02X%02X%02X", (int) (color.getRed() / (sign.getLightLevel() * 0.1)),
                (int) (color.getGreen() / (sign.getLightLevel() * 0.1)), (int) (color.getBlue() / (sign.getLightLevel() * 0.1))) : null;

        if (NMSUtil.isPaper()) {
            Component component = Component.textOfChildren(this.build(ChatComposer.adventure().decorated()));
            if (hexColor != null)
                component.colorIfAbsent(TextColor.fromHexString(hexColor));
            sign.line(lineIndex, component);
        } else {
            if (color != null) {
                ChatColor chatColor = ChatColor.of(hexColor);
                ComponentBuilder builder = new ComponentBuilder();
                BaseComponent[] components = this.buildComponents();
                for (BaseComponent component : components) {
                    builder.append(component);
                    if (component.getColorRaw() == null)
                        builder.color(chatColor);
                }
                sign.setLine(lineIndex, TextComponent.toLegacyText(builder.create()));
            } else {
                sign.setLine(lineIndex, this.build(ChatComposer.legacy()));
            }

        }
    }

    public SidedSignHelper sidedSigns() {
        return new SidedSignHelper();
    }

    public final class SidedSignHelper {
        public void setSignText(Sign sign, SignSide side, int lineIndex) {
            DyeColor signColor = sign.getColor();
            Color color = signColor != null ? signColor.getColor() : null;
            String hexColor = color != null ? String.format("#%02X%02X%02X", (int) (color.getRed() / (sign.getLightLevel() * 0.1)),
                    (int) (color.getGreen() / (sign.getLightLevel() * 0.1)), (int) (color.getBlue() / (sign.getLightLevel() * 0.1))) : null;

            if (NMSUtil.isPaper()) {
                Component component = Component.textOfChildren(MessageContents.this.build(ChatComposer.adventure().decorated()));
                if (hexColor != null)
                    component.colorIfAbsent(TextColor.fromHexString(hexColor));
                side.line(lineIndex, component);
            } else {
                if (color != null) {
                    ChatColor chatColor = ChatColor.of(hexColor);
                    ComponentBuilder builder = new ComponentBuilder();
                    BaseComponent[] components = MessageContents.this.buildComponents();
                    for (BaseComponent component : components) {
                        builder.append(component);
                        if (component.getColorRaw() == null)
                            builder.color(chatColor);
                    }
                    side.setLine(lineIndex, TextComponent.toLegacyText(builder.create()));
                } else {
                    side.setLine(lineIndex, MessageContents.this.build(ChatComposer.legacy()));
                }

            }
        }
    }

    // endregion

    // region Static constructors

    public static MessageContents fromToken(Token token) {
        return fromToken(token, new MessageOutputs());
    }

    public static MessageContents fromToken(Token token, MessageOutputs outputs) {
        return new RoseChatMessageContents(token, outputs);
    }

    public static MessageContents fromLegacy(String text) {
        return fromLegacy(text, new MessageOutputs());
    }

    public static MessageContents fromLegacy(String text, MessageOutputs outputs) {
        return new LegacyMessageContents(text, outputs);
    }

    public static MessageContents fromJson(String json) {
        return fromJson(json, new MessageOutputs());
    }

    public static MessageContents fromJson(String json, MessageOutputs outputs) {
        return new JsonMessageContents(json, outputs);
    }

    public static MessageContents fromBungee(BaseComponent[] components) {
        return fromBungee(components, new MessageOutputs());
    }

    public static MessageContents fromBungee(BaseComponent[] components, MessageOutputs outputs) {
        return new BungeeMessageContents(components, outputs);
    }

    /**
     * @return the separate Adventure chat composer instance to avoid classloader issues when running on Spigot
     */
    public static Adventure adventure() {
        return Adventure.INSTANCE;
    }

    /**
     * The separate Adventure instance to avoid classloader issues when running on Spigot
     */
    public static final class Adventure {

        public static final Adventure INSTANCE = new Adventure();

        private Adventure() {

        }

        public MessageContents fromAdventure(Component component) {
            return this.fromAdventure(component, new MessageOutputs());
        }

        public MessageContents fromAdventure(Component component, MessageOutputs outputs) {
            return new AdventureMessageContents(component, outputs);
        }

    }

    // endregion

}
