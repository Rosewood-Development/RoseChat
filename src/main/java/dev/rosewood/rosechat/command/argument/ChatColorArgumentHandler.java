package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.RoseMessage;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import java.util.ArrayList;
import java.util.List;

public class ChatColorArgumentHandler extends ArgumentHandler<String> {

    private final PermissionArea permissionArea;
    private final String permissionString;

    public ChatColorArgumentHandler(PermissionArea location) {
        super(String.class);

        this.permissionArea = location;
        this.permissionString = location == PermissionArea.NONE ?
                null : location.toString().toLowerCase();
    }

    @Override
    public String handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        if (input.equalsIgnoreCase("remove") || input.equalsIgnoreCase("off"))
            return "";

        for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
            if (!filter.colorRetention())
                continue;

            for (String match : filter.matches())
                if (match.equalsIgnoreCase(input))
                    return input;
        }

        RosePlayer player = new RosePlayer(context.getSender());
        RoseMessage message = RoseMessage.forLocation(player, this.permissionArea);
        MessageContents components = MessageTokenizer.tokenize(message, player, input, MessageDirection.PLAYER_TO_SERVER, Tokenizers.COLORS_BUNDLE);
        String plainText = components.build(ChatComposer.plain());

        // Return if the colorized string contains no color.
        if (plainText.equalsIgnoreCase(input) || !plainText.isBlank() || input.contains("&r"))
            throw new HandledArgumentException("argument-handler-chatcolor");

        return input;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        List<String> suggestions = new ArrayList<>();

        // Manually add each formatting type if the player has permission.
        suggestions.add("remove");
        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.color." + this.permissionString) && !Settings.USE_PER_COLOR_PERMISSIONS.get())
            suggestions.add("&a");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.bold." + this.permissionString))
            suggestions.add("&l");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.strikethrough." + this.permissionString))
            suggestions.add("&m");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.underline." + this.permissionString))
            suggestions.add("&n");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.italic." + this.permissionString))
            suggestions.add("&o");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.magic." + this.permissionString))
            suggestions.add("&k");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.hex." + this.permissionString))
            suggestions.add("#FFFFFF");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.rainbow." + this.permissionString))
            suggestions.add("<r:0.5>");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.gradient." + this.permissionString))
            suggestions.add("<g:#FFFFFF:#000000>");

        if (this.permissionString == null ||
                context.getSender().hasPermission("rosechat.replacements." + this.permissionString.toLowerCase())) {
            for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
                if (filter.colorRetention()
                        && filter.hasPermission(new RosePlayer(context.getSender())))
                    suggestions.addAll(filter.matches());
            }
        }

        return suggestions;
    }

}
