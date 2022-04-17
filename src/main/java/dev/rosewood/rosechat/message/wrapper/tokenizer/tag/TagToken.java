package dev.rosewood.rosechat.message.wrapper.tokenizer.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final Tag tag;
    private final String tagged;

    public TagToken(MessageWrapper wrapper, Group group, RoseSender sender, RoseSender viewer, Tag tag, String originalContent, String tagged) {
        super(sender, viewer, originalContent);
        this.messageWrapper = wrapper;
        this.group = group;
        this.tag = tag;
        this.tagged = tagged;
    }

    @Override
    public BaseComponent[] toComponents() {
        PlaceholderManager manager = RoseChatAPI.getInstance().getPlaceholderManager();
        CustomPlaceholder placeholder = manager.getPlaceholder(this.tag.getFormat());
        if (placeholder == null) return null;

        String taggedName = this.tag.getSuffix() != null ? this.tagged.replace(this.tag.getPrefix(), "").replace(this.tag.getSuffix(), "")
                : this.tagged.replace(this.tag.getPrefix(), "");
        Matcher matcher = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}]").matcher(taggedName);
        String punctuation = matcher.find() ? this.tagged.substring(matcher.start() + 1) : "";
        taggedName = this.tag.getSuffix() != null ? taggedName : taggedName.replace(punctuation, "");

        Player taggedPlayer = MessageUtils.getPlayer(taggedName);
        if (this.messageWrapper != null && taggedPlayer != null) this.messageWrapper.getTaggedPlayers().add(taggedPlayer.getUniqueId());
        if (this.messageWrapper != null && this.tag.getSound() != null) this.messageWrapper.setTagSound(this.tag.getSound());

        RoseSender tagged = taggedPlayer != null && this.tag.shouldTagOnlinePlayers() ? new RoseSender(taggedPlayer) : new RoseSender(taggedName, "default");
        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(this.getSender(), tagged, this.group)
                .addPlaceholder("tagged", taggedName).build();

        BaseComponent[] components;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        String hoverString = placeholder.getHover() != null ? placeholders.apply(placeholder.getHover().parse(this.getSender(), this.getViewer(), placeholders)) : null;
        if (hoverString != null) {
            MessageTokenizer hoverTokenizer = new MessageTokenizer.Builder()
                    .group(this.group).sender(this.getSender()).viewer(this.getViewer()).location(MessageLocation.OTHER)
                    .message(this.messageWrapper)
                    .tokenizers(Tokenizers.TAG_TOKENIZERS).tokenize(hoverString);
            BaseComponent[] hover = hoverTokenizer.toComponents();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        StringBuilder textBuilder = new StringBuilder();
        if (this.tag.shouldMatchLength()) {
            String colorlessContent = TextComponent.toPlainText(hoverEvent.getValue());
            String replacement = placeholder.getText().parse(this.getSender(), this.getViewer(), placeholders);
            for (int i = 0; i < colorlessContent.length(); i++) textBuilder.append(replacement).append("&f&r");
        } else {
            textBuilder.append(placeholder.getText().parse(this.getSender(), tagged, placeholders));
            textBuilder.append(punctuation).append("&f&r");
        }

        components = TextComponent.fromLegacyText(placeholders.apply(textBuilder.toString()));

        String clickString = placeholder.getClick() != null ? placeholders.apply(placeholder.getClick().parse(this.getSender(), this.getViewer(), placeholders)) : null;
        ClickEvent.Action action = placeholder.getClick() != null ? placeholder.getClick().parseToAction(this.getSender(), this.getViewer(), placeholders) : null;
        if (clickString != null && action != null) {
            String click = this.getSender().isPlayer() ? PlaceholderAPIHook.applyPlaceholders(this.getSender().asPlayer(), clickString) : clickString;
            clickEvent = new ClickEvent(action, HexUtils.colorify(click));
        }

        ComponentBuilder componentBuilder = new ComponentBuilder();

        for (BaseComponent component : components) {
            for (char c : component.toPlainText().toCharArray()) {
                componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE)
                        .bold(component.isBold())
                        .italic(component.isItalic())
                        .underlined(component.isUnderlined())
                        .strikethrough(component.isStrikethrough())
                        .obfuscated(component.isObfuscated())
                        .color(component.getColorRaw())
                        .event(hoverEvent)
                        .event(clickEvent);
            }
        }

        return componentBuilder.create();
    }

    public Tag getTag() {
        return this.tag;
    }
}
