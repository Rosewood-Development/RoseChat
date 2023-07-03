package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class RoseChatParser implements MessageParser {

    @Override
    public BaseComponent[] parse(RoseMessage message, RosePlayer sender, RosePlayer viewer, String format) {
        // If the message is a private message, then the viewer should be the one who received the pm.
        RosePlayer receiver;

        if (message.getMessageRules() != null) {
            receiver = message.getMessageRules().isPrivateMessage() ?
                    message.getMessageRules().getPrivateMessageInfo().getReceiver() : viewer;
        } else {
            receiver = viewer;
        }

        if (Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
            return MessageTokenizer.from(message, receiver, format,
                            Tokenizers.DISCORD_EMOJI_BUNDLE,
                            Tokenizers.MARKDOWN_BUNDLE,
                            Tokenizers.DISCORD_FORMATTING_BUNDLE,
                            Tokenizers.DEFAULT_BUNDLE).toComponents();
        } else {
            return MessageTokenizer.from(message, receiver, format,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.DEFAULT_BUNDLE).toComponents();
        }

//        ComponentBuilder componentBuilder = new ComponentBuilder();
//
//        // If there is no format, or the format does not contain "{message}", then parse without the format.
//        if (format == null || !format.contains("{message}")) {
//            if (Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, message.getMessage(), sender.isConsole(),
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.MARKDOWN_BUNDLE,
//                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                ComponentBuilder.FormatRetention.FORMATTING);
//            } else {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, message.getMessage(), sender.isConsole(),
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            }
//
//            return componentBuilder.create();
//        }
//
//        // Split the format to get the placeholders before and after the message.
//        String[] formatSplit = format.split("\\{message\\}");
//        String before = formatSplit.length > 0 ? formatSplit[0] : null;
//        String after = formatSplit.length > 1 ? formatSplit[1] : null;
//
//        if (before != null && !before.isEmpty()) {
//            if (Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, before, true,
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.MARKDOWN_BUNDLE,
//                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            } else {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, before, true,
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            }
//        }
//
//        if (format.contains("{message}")) {
//            String formatColor = message.getChatColorFromFormat(receiver, format);
//
//            if (Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, formatColor + message.getMessage(), sender.isConsole(),
//                          Tokenizers.DISCORD_EMOJI_BUNDLE,
//                          Tokenizers.MARKDOWN_BUNDLE,
//                          Tokenizers.DISCORD_FORMATTING_BUNDLE,
//                          Tokenizers.DEFAULT_BUNDLE)
//                          .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            } else {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, formatColor + message.getMessage(), sender.isConsole(),
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            }
//        }
//
//        if (after != null && !after.isEmpty()) {
//            if (Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, after, true,
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.MARKDOWN_BUNDLE,
//                                Tokenizers.DISCORD_FORMATTING_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            } else {
//                componentBuilder.append(
//                        MessageTokenizer.from(message, receiver, after, true,
//                                Tokenizers.DISCORD_EMOJI_BUNDLE,
//                                Tokenizers.DEFAULT_BUNDLE)
//                                .toComponents(),
//                        ComponentBuilder.FormatRetention.FORMATTING);
//            }
//        }
//
//        return componentBuilder.create();
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.PLAYER_TO_SERVER;
    }

}
