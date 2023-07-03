//package dev.rosewood.rosechat.message.tokenizer.replacement;
//
//import dev.rosewood.rosechat.api.RoseChatAPI;
//import dev.rosewood.rosechat.chat.ChatReplacement;
//import dev.rosewood.rosechat.message.MessageUtils;
//import dev.rosewood.rosechat.message.RosePlayer;
//import dev.rosewood.rosechat.message.tokenizer.Token;
//import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
//import dev.rosewood.rosechat.message.wrapper.RoseMessage;
//
//public class ReplacementTokenizer implements Tokenizer<Token> {
//
//    @Override
//    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
//        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
//            if (replacement.isRegex() || !input.startsWith(replacement.getText())) continue;
//            if (!ignorePermissions
//                    && !MessageUtils.hasExtendedTokenPermission(roseMessage, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
//                return null;
//
//            String originalContent = replacement.getText();
//            String content = replacement.getReplacement();
//
//            return new Token(new Token.TokenSettings(originalContent).content(content).placeholder("message", originalContent).ignoreTokenizer(this));
//        }
//
//        return null;
//    }
//
//}
