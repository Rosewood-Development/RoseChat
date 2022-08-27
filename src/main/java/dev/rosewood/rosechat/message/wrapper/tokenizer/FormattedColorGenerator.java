package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class FormattedColorGenerator {

    private final HexUtils.ColorGenerator colorGenerator;
    private boolean obfuscated, bold, strikethrough, underline, italic;
    private ChatColor previousColor;

    public FormattedColorGenerator(HexUtils.ColorGenerator colorGenerator) {
        this.colorGenerator = colorGenerator;
    }

    public void copyFormatsTo(FormattedColorGenerator colorGenerator) {
        colorGenerator.obfuscated(this.obfuscated);
        colorGenerator.bold(this.bold);
        colorGenerator.strikethrough(this.strikethrough);
        colorGenerator.underline(this.underline);
        colorGenerator.italic(this.italic);
    }

    public void obfuscated(boolean value) {
        this.obfuscated = value;
    }

    public void bold(boolean value) {
        this.bold = value;
    }

    public void strikethrough(boolean value) {
        this.strikethrough = value;
    }

    public void underline(boolean value) {
        this.underline = value;
    }

    public void italic(boolean value) {
        this.italic = value;
    }

    public void apply(ComponentBuilder componentBuilder, boolean copyPrevious) {
        if (this.colorGenerator != null) {
            ChatColor chatColor = copyPrevious ? this.previousColor : this.colorGenerator.nextChatColor();
            componentBuilder.color(chatColor);
            if (!copyPrevious)
                this.previousColor = chatColor;
        }
        componentBuilder.obfuscated(this.obfuscated);
        componentBuilder.bold(this.bold);
        componentBuilder.strikethrough(this.strikethrough);
        componentBuilder.underlined(this.underline);
        componentBuilder.italic(this.italic);
    }

    public boolean isApplicable() {
        return this.colorGenerator != null || this.obfuscated || this.bold || this.strikethrough || this.underline || this.italic;
    }

}
