package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.command.chat.BaseChatCommand;
import dev.rosewood.rosechat.command.command.BaseCommand;
import dev.rosewood.rosechat.command.command.ChannelCommand;
import dev.rosewood.rosechat.command.command.ChatColorCommand;
import dev.rosewood.rosechat.command.command.DeleteMessageCommand;
import dev.rosewood.rosechat.command.command.IgnoreCommand;
import dev.rosewood.rosechat.command.command.IgnorelistCommand;
import dev.rosewood.rosechat.command.command.MessageCommand;
import dev.rosewood.rosechat.command.command.MuteCommand;
import dev.rosewood.rosechat.command.command.NickColorCommand;
import dev.rosewood.rosechat.command.command.NicknameCommand;
import dev.rosewood.rosechat.command.command.RealnameCommand;
import dev.rosewood.rosechat.command.command.ReplyCommand;
import dev.rosewood.rosechat.command.command.SocialSpyCommand;
import dev.rosewood.rosechat.command.command.ToggleMessageCommand;
import dev.rosewood.rosechat.command.command.ToggleSoundCommand;
import dev.rosewood.rosechat.command.command.UnmuteCommand;
import dev.rosewood.rosechat.command.group.BaseGroupCommand;
import dev.rosewood.rosechat.command.group.GroupMessageCommand;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.manager.AbstractCommandManager;
import java.util.List;
import java.util.function.Function;

public class CommandManager extends AbstractCommandManager {

    public CommandManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public List<Function<RosePlugin, BaseRoseCommand>> getRootCommands() {
        return List.of(
                BaseCommand::new,
                BaseChatCommand::new,
                BaseGroupCommand::new,
                GroupMessageCommand::new,
                ChannelCommand::new,
                ChatColorCommand::new,
                DeleteMessageCommand::new,
                IgnoreCommand::new,
                IgnorelistCommand::new,
                MessageCommand::new,
                MuteCommand::new,
                NickColorCommand::new,
                NicknameCommand::new,
                RealnameCommand::new,
                ReplyCommand::new,
                SocialSpyCommand::new,
                ToggleMessageCommand::new,
                ToggleSoundCommand::new,
                UnmuteCommand::new
        );
    }

}
