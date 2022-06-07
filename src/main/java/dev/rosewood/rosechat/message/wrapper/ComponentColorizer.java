package dev.rosewood.rosechat.message.wrapper;

import com.google.gson.JsonParser;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class to colorize BaseComponents.
 */
public class ComponentColorizer {

    public static final JsonParser JSON_PARSER = new JsonParser();
    private static final int CHARS_UNTIL_LOOP = 30;
    public static final Pattern DISCORD_BOLD_MARKDOWN = Pattern.compile("\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)");
    public static final Pattern DISCORD_UNDERLINE_MARKDOWN = Pattern.compile("__([\\s\\S]+?)__(?!_)");
    public static final Pattern DISCORD_ITALIC_MARKDOWN = Pattern.compile("\\b_((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_\\b|\\*(?=\\S)((?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?)\\*(?!\\*)");
    public static final Pattern DISCORD_STRIKETHROUGH_MARKDOWN = Pattern.compile("~~(?=\\S)([\\s\\S]*?\\S)~~");
    public static final Pattern VALID_LEGACY_REGEX = Pattern.compile("&[0-9a-fA-F]");
    public static final Pattern VALID_LEGACY_REGEX_FORMATTING = Pattern.compile("&[k-oK-OrR]");
    public static final Pattern HEX_REGEX = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}");
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("<(?<type>rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?(:(?<loop>l|L|loop))?>");
    public static final Pattern GRADIENT_PATTERN = Pattern.compile("<(?<type>gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})(:(?<loop>l|L|loop))?>");
    public static final Pattern STOP = Pattern.compile(
            "<(rainbow|r)(#(\\d+))?(:(\\d*\\.?\\d+))?(:(\\d*\\.?\\d+))?(:(l|L|loop))?>|" +
                    "<(gradient|g)(#(\\d+))?((:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})(:(l|L|loop))?>|" +
                    "(&[a-f0-9r])|" +
                    "<#([A-Fa-f0-9]){6}>|" +
                    "\\{#([A-Fa-f0-9]){6}}|" +
                    "&#([A-Fa-f0-9]){6}|" +
                    "#([A-Fa-f0-9]){6}"
    );

    private ComponentColorizer() {

    }

    /**
     * Colorizes an array of base components with & colors, formatting, hex colors, and gradients.
     * @param components The components to colorize.
     * @return The colorized components.
     */
    public static BaseComponent[] colorize(BaseComponent[] components) {
        components = parseLegacyFormatting(components);
        components = parseRainbow(components);
        components = parseGradients(components);
        components = parseColors(components);
        components = parseLegacyColors(components);
        if (ConfigurationManager.Setting.USE_DISCORD_FORMATTING.getBoolean()) components = parseDiscordFormatting(components);
        return components;
    }

    private static BaseComponent[] parseRainbow(BaseComponent[] components) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        // Build the components into a string.
        for (BaseComponent component : components)
            stringBuilder.append(component.toPlainText());
        String componentContent = stringBuilder.toString();

        // Check if the content contains the rainbow pattern.
        Matcher matcher = RAINBOW_PATTERN.matcher(componentContent);
        if (matcher.find()) {

            // Retrieve parameters from the rainbow pattern.
            int speed = -1;
            float saturation = 1.0F;
            float brightness = 1.0F;
            boolean looping = getCaptureGroup(matcher, "looping") != null;

            String speedGroup = getCaptureGroup(matcher, "speed");
            if (speedGroup != null) {
                try {
                    speed = Integer.parseInt(speedGroup);
                } catch (NumberFormatException ignored) { }
            }

            String saturationGroup = getCaptureGroup(matcher, "saturation");
            if (saturationGroup != null) {
                try {
                    saturation = Float.parseFloat(saturationGroup);
                } catch (NumberFormatException ignored) { }
            }

            String brightnessGroup = getCaptureGroup(matcher, "brightness");
            if (brightnessGroup != null) {
                try {
                    brightness = Float.parseFloat(brightnessGroup);
                } catch (NumberFormatException ignored) { }
            }

            int contentStart = matcher.end();
            int contentStop = findStop(componentContent, matcher.end() - 1);
            String content = componentContent.substring(contentStart, contentStop);
            int contentLength = content.length();
            char[] chars = content.toCharArray();
            for (int i = 0; i < chars.length - 1; i++)
                if (chars[i] == '&' && "KkLlMmNnOoRr".indexOf(chars[i + 1]) > -1)
                    contentLength -= 2;

            int length = looping ? Math.min(contentLength, CHARS_UNTIL_LOOP) : contentLength;

            ColorGenerator rainbow;
            if (speed == -1) {
                rainbow = new Rainbow(length, saturation, brightness);
            } else {
                rainbow = new Rainbow(length, saturation, brightness);
            }

            for (int i = 0; i < components.length; i++) {
                if (i >= matcher.start() && i <= matcher.end() - 1) continue;

                BaseComponent component = components[i];
                if (i >= contentStart && i < contentStop) {
                    String text = component.toPlainText();
                    if (text.isEmpty()) continue;
                    if (text.charAt(0) == '&' && i + 1 < components.length) {
                        BaseComponent nextComponent = components[i + 1];
                        String nextText = nextComponent.toPlainText();
                        org.bukkit.ChatColor color = org.bukkit.ChatColor.getByChar(nextText.charAt(0));
                        if (color != null && color.isFormat()) {
                            i++;
                            continue;
                        }
                        continue;
                    }
                    int componentColor = component.getColor().getColor().getRGB();
                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent())
                            .color(componentColor == ChatColor.WHITE.getColor().getRGB() ? ChatColor.of(rainbow.next()) : component.getColorRaw())
                            .obfuscated(component.isObfuscated())
                            .bold(component.isBold())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
            }

            return parseRainbow(componentBuilder.create());
        }

        return components;
    }

    private static BaseComponent[] parseGradients(BaseComponent[] components) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        for (BaseComponent component : components)
            stringBuilder.append(component.toPlainText());
        String componentContent = stringBuilder.toString();

        Matcher matcher = GRADIENT_PATTERN.matcher(componentContent);
        if (matcher.find()) {
            int speed = -1;
            boolean looping = getCaptureGroup(matcher, "loop") != null;

            List<Color> hexSteps = Arrays.stream(getCaptureGroup(matcher, "hex").substring(1).split(":"))
                    .map(x -> x.length() != 4 ? x : String.format("#%s%s%s%s%s%s", x.charAt(1), x.charAt(1), x.charAt(2), x.charAt(2), x.charAt(3), x.charAt(3)))
                    .map(Color::decode)
                    .collect(Collectors.toList());

            String speedGroup = getCaptureGroup(matcher, "speed");
            if (speedGroup != null) {
                try {
                    speed = Integer.parseInt(speedGroup);
                } catch (NumberFormatException ignored) { }
            }

            int contentStart = matcher.end();
            int contentStop = findStop(componentContent, matcher.end() - 1);
            String content = componentContent.substring(contentStart, contentStop);
            int contentLength = content.length();
            char[] chars = content.toCharArray();
            for (int i = 0; i < chars.length - 1; i++)
                if (chars[i] == '&' && "KkLlMmNnOoRr".indexOf(chars[i + 1]) > -1)
                    contentLength -= 2;

            int length = looping ? Math.min(contentLength, CHARS_UNTIL_LOOP) : contentLength;

            ColorGenerator gradient;
            if (speed == -1) {
                gradient = new Gradient(hexSteps, length);
            } else {
                gradient = new Gradient(hexSteps, length);
            }

            for (int i = 0; i < components.length; i++) {
                if (i > matcher.start() - 1 && i < matcher.end()) continue;

                BaseComponent component = components[i];
                if (i >= contentStart && i < contentStop) {
                    String text = component.toPlainText();
                    if (text.isEmpty()) continue;
                    if (text.charAt(0) == '&' && i + 1 < components.length) {
                        BaseComponent nextComponent = components[i + 1];
                        String nextText = nextComponent.toPlainText();
                        org.bukkit.ChatColor color = org.bukkit.ChatColor.getByChar(nextText.charAt(0));
                        if (color != null && color.isFormat()) {
                            i++;
                            continue;
                        }
                        continue;
                    }

                    int componentColor = component.getColor().getColor().getRGB();
                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                            .event(component.getHoverEvent()).event(component.getClickEvent())
                            .color(componentColor == ChatColor.WHITE.getColor().getRGB() ? ChatColor.of(gradient.next()) : component.getColorRaw())
                            .obfuscated(component.isObfuscated())
                            .bold(component.isBold())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
            }

            return parseGradients(componentBuilder.create());
        }

        return components;
    }

    private static BaseComponent[] parseLegacyColors(BaseComponent[] components) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        for (BaseComponent component : components)
            stringBuilder.append(component.toPlainText());
        String componentContent = stringBuilder.toString();

        Matcher matcher = VALID_LEGACY_REGEX.matcher(componentContent);
        if (matcher.find()) {
            int contentStart = matcher.end();
            int contentStop = findStop(componentContent, matcher.end() - 1);

            for (int i = 0; i < components.length; i++) {
                if (i > matcher.start() - 1 && i < matcher.end()) continue;

                BaseComponent component = components[i];
                if (i >= contentStart && i < contentStop + 2) {
                    String text = component.toPlainText();
                    int componentColor = component.getColor().getColor().getRGB();

                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                            .event(component.getHoverEvent()).event(component.getClickEvent())
                            .color(componentColor == ChatColor.WHITE.getColor().getRGB() ? ChatColor.getByChar(componentContent.charAt(matcher.end() - 1)) : component.getColorRaw())
                            .obfuscated(component.isObfuscated())
                            .bold(component.isBold())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
            }

            return parseLegacyColors(componentBuilder.create());
        }

        return components;
    }

    private static BaseComponent[] parseColors(BaseComponent[] components) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        for (BaseComponent component : components)
            stringBuilder.append(component.toPlainText());
        String componentContent = stringBuilder.toString();

        Matcher matcher = HEX_REGEX.matcher(componentContent);
        if (matcher.find()) {
            int contentStart = matcher.end();
            int contentStop = findStop(componentContent, matcher.end() - 1);

            for (int i = 0; i < components.length; i++) {
                if (i > matcher.start() - 1 && i < matcher.end()) continue;

                BaseComponent component = components[i];
                if (i >= contentStart && i < contentStop + 2) {
                    String text = component.toPlainText();
                    int componentColor = component.getColor().getColor().getRGB();
                    String hexColor = parseHexColor(componentContent.substring(matcher.start(), matcher.end()));

                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                            .event(component.getHoverEvent()).event(component.getClickEvent())
                            .color(componentColor == ChatColor.WHITE.getColor().getRGB() ? ChatColor.of(hexColor) : component.getColorRaw())
                            .obfuscated(component.isObfuscated())
                            .bold(component.isBold())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
            }

            return parseColors(componentBuilder.create());
        }

        return components;
    }

    private static BaseComponent[] parseLegacyFormatting(BaseComponent[] components) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        for (BaseComponent component : components)
            stringBuilder.append(component.toPlainText());
        String componentContent = stringBuilder.toString();

        Matcher matcher = VALID_LEGACY_REGEX_FORMATTING.matcher(componentContent);
        if (matcher.find()) {
            int contentStart = matcher.end();
            String content = componentContent.substring(matcher.start(), matcher.end());

            for (int i = 0; i < components.length; i++) {
                if (i > matcher.start() - 1 && i < matcher.end()) continue;

                BaseComponent component = components[i];
                if (i >= contentStart) {
                    String text = component.toPlainText();
                    switch (content.toLowerCase().charAt(1)) {
                        case 'k':
                            componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                                    .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                                    .obfuscated(true)
                                    .bold(component.isBold())
                                    .underlined(component.isUnderlined())
                                    .strikethrough(component.isStrikethrough())
                                    .italic(component.isItalic());
                            break;
                        case 'l':
                            componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                                    .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                                    .obfuscated(component.isObfuscated())
                                    .bold(true)
                                    .underlined(component.isUnderlined())
                                    .strikethrough(component.isStrikethrough())
                                    .italic(component.isItalic());
                            break;
                        case 'm':
                            componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                                    .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                                    .obfuscated(component.isObfuscated())
                                    .bold(component.isBold())
                                    .underlined(component.isUnderlined())
                                    .strikethrough(true)
                                    .italic(component.isItalic());
                            break;
                        case 'n':
                            componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                                    .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                                    .obfuscated(component.isObfuscated())
                                    .bold(component.isBold())
                                    .underlined(true)
                                    .strikethrough(component.isStrikethrough())
                                    .italic(component.isItalic());
                            break;
                        case 'o':
                            componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                                    .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                                    .obfuscated(component.isObfuscated())
                                    .bold(component.isBold())
                                    .underlined(component.isUnderlined())
                                    .strikethrough(component.isStrikethrough())
                                    .italic(true);
                            break;
                        case 'r':
                            componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFontRaw())
                                    .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                                    .obfuscated(false).bold(false).underlined(false).strikethrough(false).italic(false);
                            break;
                    }
                    continue;
                }

                componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
            }

            return parseLegacyFormatting(componentBuilder.create());
        }

        return components;
    }

    private static BaseComponent[] parseDiscordFormatting(BaseComponent[] components) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        for (BaseComponent component : components)
            stringBuilder.append(component.toPlainText());
        String componentContent = stringBuilder.toString();

        Matcher boldMatcher = DISCORD_BOLD_MARKDOWN.matcher(componentContent);
        if (boldMatcher.find()) {
            for (int i = 0; i < components.length; i++) {
                if (i < boldMatcher.start() || i > boldMatcher.end() - 1) {
                    BaseComponent component = components[i];
                    componentBuilder.append(components[i], ComponentBuilder.FormatRetention.NONE)
                            .font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                if (i > boldMatcher.start() + 1 && i < boldMatcher.end() - 2) {
                    BaseComponent component = components[i];
                    String text = component.toPlainText();
                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(true)
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                }
            }

            return parseDiscordFormatting(componentBuilder.create());
        }

        Matcher underlineMatcher = DISCORD_UNDERLINE_MARKDOWN.matcher(componentContent);
        if (underlineMatcher.find()) {
            for (int i = 0; i < components.length; i++) {
                if (i < underlineMatcher.start() || i > underlineMatcher.end() - 1) {
                    BaseComponent component = components[i];
                    componentBuilder.append(components[i], ComponentBuilder.FormatRetention.NONE)
                            .font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                if (i > underlineMatcher.start() + 1 && i < underlineMatcher.end() - 2) {
                    BaseComponent component = components[i];
                    String text = component.toPlainText();
                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(true)
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                }
            }

            return parseDiscordFormatting(componentBuilder.create());
        }

        Matcher italicMatcher = DISCORD_ITALIC_MARKDOWN.matcher(componentContent);
        if (italicMatcher.find()) {
            for (int i = 0; i < components.length; i++) {
                if (i < italicMatcher.start() || i > italicMatcher.end() - 1) {
                    BaseComponent component = components[i];
                    componentBuilder.append(components[i], ComponentBuilder.FormatRetention.NONE)
                            .font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                if (i > italicMatcher.start() && i < italicMatcher.end() - 1) {
                    BaseComponent component = components[i];
                    String text = component.toPlainText();
                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(true);
                }
            }

            return parseDiscordFormatting(componentBuilder.create());
        }

        Matcher strikethroughMatcher = DISCORD_STRIKETHROUGH_MARKDOWN.matcher(componentContent);
        if (strikethroughMatcher.find()) {
            for (int i = 0; i < components.length; i++) {
                if (i < strikethroughMatcher.start() || i > strikethroughMatcher.end() - 1) {
                    BaseComponent component = components[i];
                    componentBuilder.append(components[i], ComponentBuilder.FormatRetention.NONE)
                            .font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(component.isStrikethrough())
                            .italic(component.isItalic());
                    continue;
                }

                if (i > strikethroughMatcher.start() + 1 && i < strikethroughMatcher.end() - 2) {
                    BaseComponent component = components[i];
                    String text = component.toPlainText();
                    componentBuilder.append(text, ComponentBuilder.FormatRetention.NONE).font(component.getFont())
                            .event(component.getHoverEvent()).event(component.getClickEvent()).color(component.getColorRaw())
                            .bold(component.isBold())
                            .obfuscated(component.isObfuscated())
                            .underlined(component.isUnderlined())
                            .strikethrough(true)
                            .italic(component.isItalic());
                }
            }

            return parseDiscordFormatting(componentBuilder.create());
        }

        return components;
    }

    public static String parseDiscordFormatting(String str) {
        Matcher boldMatcher = DISCORD_BOLD_MARKDOWN.matcher(str);
        if (boldMatcher.find()) {
            str = str.replaceFirst("\\*\\*", "&l").replaceFirst("\\*\\*", "");
        }

        Matcher italicMatcher = DISCORD_ITALIC_MARKDOWN.matcher(str);
        if (italicMatcher.find()) {
            str = str.replaceFirst("\\*", "&o").replaceFirst("\\*", "");
        }

        Matcher underlineMatcher = DISCORD_UNDERLINE_MARKDOWN.matcher(str);
        if (underlineMatcher.find()) {
            str = str.replaceFirst("__", "&n").replaceFirst("__", "");
        }

        Matcher strikethroughMatcher = DISCORD_STRIKETHROUGH_MARKDOWN.matcher(str);
        if (strikethroughMatcher.find()) {
            str = str.replaceFirst("~~", "&m").replaceFirst("~~", "");
        }

        return str;
    }

    private static String parseHexColor(String color) {
        char indicator = color.charAt(0);
        switch (indicator) {
            case '<':
            case '{':
                color = color.substring(0, color.length() - 1);
            case '&':
                color = color.substring(1);
        }

        if (color.length() == 4) {
            color = String.format("#%s%s%s%s%s%s", color.charAt(1), color.charAt(1), color.charAt(2), color.charAt(2), color.charAt(3), color.charAt(3));
        } else if (color.length() == 8) {
            color = color.substring(1);
        }

        return color;
    }

    private static String getCaptureGroup(Matcher matcher, String group) {
        try {
            return matcher.group(group);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
    }

    private static int findStop(String content, int searchAfter) {
        Matcher matcher = STOP.matcher(content);
        while (matcher.find()) {
            if (matcher.start() > searchAfter)
                return matcher.start();
        }
        return content.length();
    }

    private interface ColorGenerator {
        Color next();
    }

    public static class Rainbow implements ColorGenerator {

        protected final float hueStep, saturation, brightness;
        protected float hue;

        public Rainbow(int totalColors, float saturation, float brightness) {
            if (totalColors < 1)
                totalColors = 1;

            this.hueStep = 1.0F / totalColors;
            this.saturation = Math.max(0, Math.min(1, saturation));
            this.brightness = Math.max(0, Math.min(1, brightness));
            this.hue = 0;
        }

        @Override
        public Color next() {
            Color color = Color.getHSBColor(this.hue, this.saturation, this.brightness);
            this.hue += this.hueStep;
            return color;
        }
    }

    public static class Gradient implements ColorGenerator {

        private final List<TwoStopGradient> gradients;
        private final int steps;
        protected long step;

        public Gradient(List<Color> colors, int steps) {
            if (colors.size() < 2)
                throw new IllegalArgumentException("Must provide at least 2 colors");

            this.gradients = new ArrayList<>();
            this.steps = steps - 1;
            this.step = 0;

            float increment = (float) this.steps / (colors.size() - 1);
            for (int i = 0; i < colors.size() - 1; i++)
                this.gradients.add(new TwoStopGradient(colors.get(i), colors.get(i + 1), increment * i, increment * (i + 1)));
        }

        @Override
        public Color next() {
            if (NMSUtil.getVersionNumber() < 16 || this.steps <= 1)
                return this.gradients.get(0).colorAt(0);

            int adjustedStep = (int) Math.round(Math.abs(((2 * Math.asin(Math.sin(this.step * (Math.PI / (2 * this.steps))))) / Math.PI) * this.steps));

            Color color;
            if (this.gradients.size() < 2) {
                color = this.gradients.get(0).colorAt(adjustedStep);
            } else {
                float segment = (float) this.steps / this.gradients.size();
                int index = (int) Math.min(Math.floor(adjustedStep / segment), this.gradients.size() - 1);
                color = this.gradients.get(index).colorAt(adjustedStep);
            }

            this.step++;
            return color;
        }
    }

    private static class TwoStopGradient {

        private final Color startColor;
        private final Color endColor;
        private final float lowerRange;
        private final float upperRange;

        private TwoStopGradient(Color startColor, Color endColor, float lowerRange, float upperRange) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.lowerRange = lowerRange;
            this.upperRange = upperRange;
        }

        public Color colorAt(int step) {
            return new Color(
                    this.calculateHexPiece(step, this.startColor.getRed(), this.endColor.getRed()),
                    this.calculateHexPiece(step, this.startColor.getGreen(), this.endColor.getGreen()),
                    this.calculateHexPiece(step, this.startColor.getBlue(), this.endColor.getBlue())
            );
        }

        private int calculateHexPiece(int step, int channelStart, int channelEnd) {
            float range = this.upperRange - this.lowerRange;
            float interval = (channelEnd - channelStart) / range;
            return Math.round(interval * (step - this.lowerRange) + channelStart);
        }

    }
}
