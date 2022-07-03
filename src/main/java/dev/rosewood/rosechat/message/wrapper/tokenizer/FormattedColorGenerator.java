package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class FormattedColorGenerator {

    private final HexUtils.ColorGenerator colorGenerator;
    private boolean obfuscated, bold, strikethrough, underline, italic;

    public FormattedColorGenerator(HexUtils.ColorGenerator colorGenerator) {
        this.colorGenerator = colorGenerator;
    }

    public void copyFormatsTo(FormattedColorGenerator colorGenerator) {
        if (this.obfuscated)
            colorGenerator.obfuscated();
        if (this.bold)
            colorGenerator.bold();
        if (this.strikethrough)
            colorGenerator.strikethrough();
        if (this.underline)
            colorGenerator.underline();
        if (this.italic)
            colorGenerator.italic();
    }

    public void obfuscated() {
        this.obfuscated = true;
    }

    public void bold() {
        this.bold = true;
    }

    public void strikethrough() {
        this.strikethrough = true;
    }

    public void underline() {
        this.underline = true;
    }

    public void italic() {
        this.italic = true;
    }

    public void apply(ComponentBuilder componentBuilder) {
        if (this.colorGenerator != null)
            componentBuilder.color(this.colorGenerator.nextChatColor());
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
