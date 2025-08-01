package dev.rosewood.rosechat.message.tokenizer.decorator;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.awt.Color;
import java.util.function.Function;
import net.md_5.bungee.api.ChatColor;

public record ShadowColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction, boolean solid) implements TokenDecorator {

    public static final boolean VALID_VERSION = NMSUtil.getVersionNumber() > 21 || (NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 5);

    public ShadowColorDecorator(Function<Integer, HexUtils.ColorGenerator> colorGeneratorFunction) {
        this(colorGeneratorFunction, false);
    }

    public ShadowColorDecorator(ChatColor chatColor) {
        this(x -> new SolidColorGenerator(chatColor), true);
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.STYLING;
    }

    @Override
    public boolean isOverwrittenBy(TokenDecorator newDecorator) {
        if (newDecorator instanceof FormatDecorator formatDecorator)
            return formatDecorator.formatType() == FormatDecorator.FormatType.RESET;

        return newDecorator instanceof ShadowColorDecorator;
    }

    @Override
    public boolean isMarker() {
        return !VALID_VERSION;
    }

    @Override
    public boolean blocksTextStitching() {
        return !this.solid;
    }

    private record SolidColorGenerator(ChatColor chatColor) implements HexUtils.ColorGenerator {

        @Override
        public ChatColor nextChatColor() {
            return this.chatColor;
        }

        @Override
        public Color nextColor() {
            return this.chatColor.getColor();
        }

    }

}
