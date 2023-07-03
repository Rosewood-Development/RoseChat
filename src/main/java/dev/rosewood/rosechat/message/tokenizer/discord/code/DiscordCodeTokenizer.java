//package dev.rosewood.rosechat.message.tokenizer.discord.code;
//
//import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
//import dev.rosewood.rosechat.message.MessageUtils;
//import dev.rosewood.rosechat.message.wrapper.RoseMessage;
//import dev.rosewood.rosechat.message.RosePlayer;
//import dev.rosewood.rosechat.message.tokenizer.Token;
//import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
//
//public class DiscordCodeTokenizer implements Tokenizer<Token> {
//
//    @Override
//    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
//        if (!input.startsWith("`")) return null;
//        if (!ignorePermissions && !MessageUtils.hasTokenPermission(roseMessage, "rosechat.code")) return null;
//
//        int lastIndex = 0;
//
//        char[] chars = input.toCharArray();
//        for (int i = 1; i < chars.length; i++) {
//            if (chars[i] == '`') {
//                lastIndex = i;
//                break;
//            }
//        }
//
//        if (lastIndex == 0) return null;
//        String originalContent = input.substring(0, lastIndex + 1);
//        String content = input.substring(1, lastIndex);
//
//        String format = Setting.MARKDOWN_FORMAT_CODE_BLOCK_ONE.getString();
//        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;
//
//        return new Token(new Token.TokenSettings(originalContent).content(content).ignoreTokenizer(this));
//    }
//
//}
