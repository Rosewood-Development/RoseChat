package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class DeleteMessageCommand extends RoseChatCommand {

    public DeleteMessageCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("delmsg")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .optional("uuid", RoseChatArgumentHandlers.UUID)
                        .optional("channel", ArgumentHandlers.STRING)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, UUID uuid) {
        this.execute(new RosePlayer(context.getSender()), uuid, null);
    }

    @RoseExecutable
    public void execute(CommandContext context, UUID uuid, String channel) {
        this.execute(new RosePlayer(context.getSender()), uuid, channel);
    }

    private void execute(RosePlayer player, UUID uuid, String channelId) {
        boolean isClient = false;
        PlayerData data = player.getPlayerData();
        for (DeletableMessage message : data.getMessageLog().getDeletableMessages()) {
            if (message.getUUID().equals(uuid) && message.isClient()) {
                isClient = true;
                break;
            }
        }

        // Delete the message for the player if client, or for all players if server.
        if (isClient) {
            this.getAPI().deleteMessage(player, uuid);
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                this.getAPI().deleteMessage(new RosePlayer(onlinePlayer), uuid);
        }

        if (channelId == null)
            return;

        Channel channel = this.getAPI().getChannelById(channelId);
        if (channel == null)
            return;

        // Delete the message across linked servers too.
        for (String server : channel.getServers())
            this.getAPI().getBungeeManager().sendMessageDeletion(server, uuid);
    }

}
