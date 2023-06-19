package dev.rosewood.rosechat.message.tokenizer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageTokenizer {

    private static final Cache<TokenKey, List<Token>> TOKEN_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    private final DebugManager debugManager;
    private final List<Tokenizer<?>> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final List<Token> tokens;
    private final boolean ignorePermissions;

    public MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String message) {
        this(roseMessage, viewer, message, false, Tokenizers.DEFAULT_BUNDLE);
    }

    public MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String message, String... tokenizerBundles) {
        this(roseMessage, viewer, message, false, tokenizerBundles);
    }

    public MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String message, boolean ignorePermissions, String... tokenizerBundles) {
        this.debugManager = RoseChat.getInstance().getManager(DebugManager.class);

        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokens = new ArrayList<>();
        this.ignorePermissions = ignorePermissions;
        this.tokenizers = Arrays.stream(tokenizerBundles).flatMap(x -> Tokenizers.getBundleValues(x).stream()).distinct().collect(Collectors.toList());
        this.debugManager.addMessage(() -> "Tokenizing New Message: " + message + " for " + viewer.getName());
        this.tokens.addAll(this.tokenizeContent(parseReplacements(message, ignorePermissions), true, 0, null));
        this.debugManager.addMessage(() -> "Completed Tokenizing: " + message + " for " + viewer.getName() + "\n\n\n");
    }

    // Parse replacements before the tokenizing to allow some replacements, such as custom colours, to work properly.
    private String parseReplacements(String message, boolean ignorePermissions) {
        this.debugManager.addMessage(() -> "    Parsing Replacements...");
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex() || !message.contains(replacement.getText())) continue;
            if (!MessageUtils.isMessageEmpty(replacement.getReplacement())) continue;
            if (!ignorePermissions && !MessageUtils.hasExtendedTokenPermission(this.roseMessage, "rosechat.replacements", "rosechat.replacement." + replacement.getId())) continue;
            message = message.replace(replacement.getText(), replacement.getReplacement());
        }

        return message;
    }

    private List<Token> tokenizeContent(String content, boolean tokenizeHover, int depth, Token parent) {
        content = this.parseReplacements(content, parent != null);
        this.debugManager.addMessage(() -> "    Tokenizing Content... Parent: " + (parent == null ? "none" : parent.toString()));

        // Check cache first
        TokenKey tokenKey = new TokenKey(content, parent == null ? null : parent.clone());
        List<Token> cachedResult = TOKEN_CACHE.getIfPresent(tokenKey);
        if (cachedResult != null) {
            RosePlayer player = this.roseMessage.getSender();

            for (String permission : tokenKey.getPermissions().keySet()) {
                if (player.hasPermission(permission) == tokenKey.getPermissions().get(permission))
                    return cachedResult;
            }
        }

        List<Token> added = new ArrayList<>();
        for (int i = 0; i < content.length(); i++) {
            String substring = content.substring(i);

            for (Tokenizer<?> tokenizer : this.tokenizers) {
                if (parent != null && parent.getIgnoredTokenizers().contains(tokenizer))
                    continue;

                this.debugManager.addMessage(() -> "        Starting Tokenizing " + tokenizer.getClass().getSimpleName() + "...");

                Token token = tokenizer.tokenize(this.roseMessage, this.viewer, substring, parent != null || this.ignorePermissions);
                if (token != null) {
                    this.debugManager.addMessage(() -> "        Completed Tokenizing " + tokenizer.getClass().getSimpleName() + ", " + token + ", " + token.getOriginalContent() + " -> " + token.getContent());


                    i += token.getOriginalContent().length() - 1;
                    if (depth > 15) {
                        RoseChat.getInstance().getLogger().warning("Exceeded a depth of 15 when tokenizing message. This is probably due to infinite recursion somewhere: " + this.roseMessage.getMessage());
                        this.debugManager.addMessage(() -> "Infinite recursion detected: Message: " + this.roseMessage.getMessage()
                                + ", Original: " + token.getOriginalContent() + ", Content: " + token.getContent() + ", Token: " + token);
                        continue;
                    }

                    if (token.requiresTokenizing()) {
                        // Inherit things from parent
                        if (parent != null)
                            parent.applyInheritance(token);

                        added.add(token);
                        token.addChildren(this.tokenizeContent(token.getContent(), true, depth + 1, token));
                    } else {
                        added.add(token);
                    }

                    if (tokenizeHover && token.getHover() != null && !token.getHover().isEmpty())
                        token.addHoverChildren(this.tokenizeContent(token.getHover(), false, depth + 1, token));

                    break;
                }
            }
        }

        // Cache the result if allowed
        if (added.stream().allMatch(Token::allowsCaching)) {
            tokenKey.permissions = this.roseMessage.getSender().getCachedPermissions();
            this.roseMessage.getSender().getCachedPermissions().clear();
            TOKEN_CACHE.put(tokenKey, added.stream().map(Token::clone).collect(Collectors.toList()));
        }

        return added;
    }

    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        this.toComponents(componentBuilder, new FormattedColorGenerator(null), this.tokens);

        // Appends an empty string to always have something in the component.
        if (componentBuilder.getParts().isEmpty()) componentBuilder.append("", ComponentBuilder.FormatRetention.FORMATTING);
        return ComponentSimplifier.simplify(componentBuilder.create());
    }

    private void toComponents(ComponentBuilder componentBuilder, FormattedColorGenerator colorGenerator, List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (!token.getChildren().isEmpty()) {
                this.toComponents(componentBuilder, colorGenerator, token.getChildren());
                continue;
            }

            if (token.hasColorGenerator() || token.hasFormatCodes()) {
                List<Token> futureTokens = tokens.subList(i, tokens.size());
                if (token.hasColorGenerator()) {
                    FormattedColorGenerator newColorGenerator = new FormattedColorGenerator(token.getColorGenerator(futureTokens));
                    colorGenerator.copyFormatsTo(newColorGenerator);
                    colorGenerator = newColorGenerator;
                }

                if (token.hasFormatCodes()) {
                    PlayerData senderData = this.roseMessage.getSender().getPlayerData();
                    String color;
                    if (senderData != null) {
                        color = senderData.getColor();
                        if (color == null || color.isEmpty())
                            color = "&f";
                    } else {
                        color = "&f";
                    }

                    FormattedColorGenerator overrideColorGenerator = token.applyFormatCodes(colorGenerator, color, futureTokens);
                    if (overrideColorGenerator != null)
                        colorGenerator = overrideColorGenerator;
                }
            }

            if (!colorGenerator.isApplicable()) {
                componentBuilder.append(token.getContent(), token.shouldRetainColour() ? ComponentBuilder.FormatRetention.FORMATTING : ComponentBuilder.FormatRetention.NONE);
                if (NMSUtil.getVersionNumber() >= 16) componentBuilder.font(token.getEffectiveFont());

                if (token.getHover() != null) {
                    if (token.getHoverChildren().isEmpty()) {
                        componentBuilder.event(new HoverEvent(token.getHoverAction(), TextComponent.fromLegacyText(token.getHover())));
                    } else {
                        ComponentBuilder hoverBuilder = new ComponentBuilder();
                        this.toComponents(hoverBuilder, new FormattedColorGenerator(null), token.getHoverChildren());
                        componentBuilder.event(new HoverEvent(token.getHoverAction(), hoverBuilder.create()));
                    }
                }

                if (token.getClick() != null)
                    componentBuilder.event(new ClickEvent(token.getClickAction(), PlaceholderAPIHook.applyPlaceholders(this.roseMessage.getSender().asPlayer(), MessageUtils.getSenderViewerPlaceholders(this.roseMessage.getSender(), this.viewer).build().apply(token.getClick()))));
            } else {
                // Make sure to apply the color even if there's no content
                if (token.getContent().isEmpty()) {
                    componentBuilder.append("", token.shouldRetainColour() ? ComponentBuilder.FormatRetention.ALL : ComponentBuilder.FormatRetention.FORMATTING);
                    colorGenerator.apply(componentBuilder, false);
                    continue;
                }

                for (char c : token.getContent().toCharArray()) {
                    componentBuilder.append(String.valueOf(c), token.shouldRetainColour() ? ComponentBuilder.FormatRetention.ALL : ComponentBuilder.FormatRetention.FORMATTING);
                    if (NMSUtil.getVersionNumber() >= 16) componentBuilder.font(token.getEffectiveFont());

                    colorGenerator.apply(componentBuilder, Character.isSpaceChar(c));

                    if (token.getHover() != null) {
                        if (token.getHoverChildren().isEmpty()) {
                            componentBuilder.event(new HoverEvent(token.getHoverAction(), TextComponent.fromLegacyText(token.getHover())));
                        } else {
                            ComponentBuilder hoverBuilder = new ComponentBuilder();
                            this.toComponents(hoverBuilder, new FormattedColorGenerator(null), token.getHoverChildren());
                            componentBuilder.event(new HoverEvent(token.getHoverAction(), hoverBuilder.create()));
                        }
                    }

                    if (token.getClick() != null)
                        componentBuilder.event(new ClickEvent(token.getClickAction(), PlaceholderAPIHook.applyPlaceholders(this.roseMessage.getSender().asPlayer(), MessageUtils.getSenderViewerPlaceholders(this.roseMessage.getSender(), this.viewer).build().apply(token.getClick()))));
                }
            }
        }
    }

    private static class TokenKey {
        private final String content;
        private final Token parent;
        private Map<String, Boolean> permissions;

        public TokenKey(String content, Token parent) {
            this.content = content;
            this.parent = parent;
            this.permissions = new HashMap<>();
        }

        public Map<String, Boolean> getPermissions() {
            return this.permissions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            TokenKey tokenKey = (TokenKey) o;
            return this.content.equals(tokenKey.content) && Objects.equals(this.parent, tokenKey.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.content, this.parent);
        }
    }

}
