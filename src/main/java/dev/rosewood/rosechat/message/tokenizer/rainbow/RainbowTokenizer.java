//package dev.rosewood.rosechat.message.tokenizer.rainbow;
//
//import dev.rosewood.rosechat.message.MessageUtils;
//import dev.rosewood.rosechat.message.wrapper.RoseMessage;
//import dev.rosewood.rosechat.message.RosePlayer;
//import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
//import java.util.regex.Matcher;
//
//public class RainbowTokenizer implements Tokenizer<RainbowToken> {
//
//    @Override
//    public RainbowToken tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
//        if (!input.startsWith("<"))
//            return null;
//
//        // Check if the content contains the rainbow pattern.
//        Matcher matcher = MessageUtils.RAINBOW_PATTERN.matcher(input);
//        if (matcher.find()) {
//            if (matcher.start() != 0) return null;
//            // Retrieve parameters from the rainbow pattern.
//            float saturation = 1.0F;
//            float brightness = 1.0F;
//
//            String saturationGroup = MessageUtils.getCaptureGroup(matcher, "saturation");
//            if (saturationGroup != null) {
//                try {
//                    saturation = Float.parseFloat(saturationGroup);
//                } catch (NumberFormatException ignored) { }
//            }
//
//            String brightnessGroup = MessageUtils.getCaptureGroup(matcher, "brightness");
//            if (brightnessGroup != null) {
//                try {
//                    brightness = Float.parseFloat(brightnessGroup);
//                } catch (NumberFormatException ignored) { }
//            }
//
//            int speed = 0;
//            String speedGroup = matcher.group("speed");
//            if (speedGroup != null) {
//                try {
//                    speed = Integer.parseInt(speedGroup);
//                } catch (NumberFormatException ignored) {}
//            }
//
//            return ignorePermissions || MessageUtils.hasDefaultColor(input, roseMessage) || MessageUtils.hasTokenPermission(roseMessage, "rosechat.rainbow") ?
//                    new RainbowToken(input.substring(matcher.start(), matcher.end()), saturation, brightness, speed)
//                    : new RainbowToken(input.substring(matcher.start(), matcher.end()), 0.0f, 0.0f, speed);
//        }
//
//        return null;
//    }
//
//}
