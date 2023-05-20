package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class FromDiscordParser implements MessageParser {

    @Override
    public BaseComponent[] parse(RoseMessage message, RosePlayer sender, RosePlayer viewer, String format) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        // If there is no format, or the format does not contain "{message}", then parse without the format.
        if (format == null || !format.contains("{message}")) {
            if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, message.getMessage(), sender.isConsole(),
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.MARKDOWN_BUNDLE,
                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            } else {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, message.getMessage(), sender.isConsole(),
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            }

            return componentBuilder.create();
        }

        // Split the format to get the placeholders before and after the message.
        String[] formatSplit = format.split("\\{message\\}");
        String before = formatSplit.length > 0 ? formatSplit[0] : null;
        String after = formatSplit.length > 1 ? formatSplit[1] : null;

        if (before != null && !before.isEmpty()) {
            if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, before, true,
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.MARKDOWN_BUNDLE,
                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            } else {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, before, true,
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            }
        }

        if (format.contains("{message}")) {
            String formatColor = message.getChatColorFromFormat(viewer, format);

            if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, formatColor + message.getMessage(), sender.isConsole(),
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.MARKDOWN_BUNDLE,
                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            } else {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, formatColor + message.getMessage(), sender.isConsole(),
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            }
        }

        if (after != null && !after.isEmpty()) {
            if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, after, true,
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.MARKDOWN_BUNDLE,
                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            } else {
                componentBuilder.append(
                        new MessageTokenizer(message, viewer, after, true,
                                Tokenizers.DISCORD_EMOJI_BUNDLE,
                                Tokenizers.FROM_DISCORD_BUNDLE,
                                Tokenizers.DEFAULT_BUNDLE)
                                .toComponents(),
                        ComponentBuilder.FormatRetention.FORMATTING);
            }
        }

        return componentBuilder.create();
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.FROM_DISCORD;
    }

}
