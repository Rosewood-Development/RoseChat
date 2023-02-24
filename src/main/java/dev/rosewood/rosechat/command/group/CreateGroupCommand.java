package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupCommand extends AbstractCommand {

    public CreateGroupCommand() {
        super(true, "create");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        if (this.getAPI().getGroupChatByOwner(player.getUniqueId()) != null) {
            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-create-fail");
            return;
        }

        String id = args[0];
        if (this.getAPI().getGroupChatById(id) != null) {
            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-already-exists");
            return;
        }

        String name = getAllArgs(1, args);

        if (!MessageUtils.canColor(sender, name, "group")) return;

        RosePlayer rosePlayer = new RosePlayer(player);
       // RoseMessage message = new RoseMessage(rosePlayer, MessageLocation.GROUP, null, name).filterLanguage();
        //if (!message.canBeSent()) {
       //     if (message.getFilterType() != null) message.getFilterType().sendWarning(rosePlayer);
      //      return;
      //  }

        GroupChannel groupChat = this.getAPI().createGroupChat(id, player.getUniqueId());

        // Reset colour & formatting so uncoloured names don't take colour from previous words.
       // name = "&f&r" + message.getMessage() + "&f&r";
       // groupChat.setName(name);
        this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-create-success", StringPlaceholders.single("name", name));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) tab.add("<id>");
        if (args.length == 2) tab.add("<display name>");
        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.create";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-create-usage");
    }

}
