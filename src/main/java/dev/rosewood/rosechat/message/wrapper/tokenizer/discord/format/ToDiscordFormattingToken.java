package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.format;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class ToDiscordFormattingToken extends Token {

    private String text;
    private final List<Character> formattingCodes;

    public ToDiscordFormattingToken(RoseSender sender, RoseSender viewer, String originalContent) {
        super(sender, viewer, originalContent);
        this.text = this.getOriginalContent();
        this.formattingCodes = new ArrayList<>(Arrays.asList('l', 'm', 'n', 'o'));
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        // Remove all colour codes first
        this.text = this.text.replaceAll(ComponentColorizer.VALID_LEGACY_REGEX.pattern(), "");

        int start = 0;
        for (int i = 0; i < this.text.length(); i++) {
            char c = this.text.charAt(i);
            if (i == 0 || (this.text.charAt(i - 1) == '&' && this.formattingCodes.contains(c))) continue;
            if (i != this.text.length() - 1 && (c == '&' && this.formattingCodes.contains(this.text.charAt(i + 1)))) continue;
            start = i;
            break;
        }

        boolean hasBold = false;
        boolean hasStriketrough = false;
        boolean hasUnderline = false;
        boolean hasItalic = false;
        Matcher matcher = ComponentColorizer.VALID_LEGACY_REGEX_FORMATTING.matcher(this.text);
        while (matcher.find()) {
            switch (matcher.group().charAt(1)) {
                case 'l':
                    hasBold = true;
                    break;
                case 'm':
                    hasStriketrough = true;
                    break;
                case 'n':
                    hasUnderline = true;
                    break;
                case 'o':
                    hasItalic = true;
                    break;
            }

        }

        this.text = this.text.substring(start);

        // This order for formatting discord messages properly
        if (hasUnderline) {
            this.text = "__" + this.text;
            this.text += "__";
        }

        if (hasStriketrough) {
            this.text = "~~" + this.text;
            this.text += "~~";
        }

        if (hasItalic) {
            this.text = "*" + this.text;
            this.text += "*";
        }

        if (hasBold) {
            this.text = "**" + this.text;
            this.text += "**";
        }

        for (char c : this.text.toCharArray()) {
            componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
        }

        return componentBuilder.create();
    }
}