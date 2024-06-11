package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;

public class DebugCommand extends RoseChatCommand {

    public DebugCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("debug")
                .descriptionKey("command-debug-description")
                .permission("rosechat.debug")
                .arguments(ArgumentsDefinition.builder()
                        .optional("options", RoseChatArgumentHandlers.DEBUG_OPTIONS)
                        .build())
                .build();
    }

    // todo wait for rg update
    @RoseExecutable
    public void execute(CommandContext context) {
        DebugManager debugManager = this.rosePlugin.getManager(DebugManager.class);

        if (debugManager.isEnabled()) {
            debugManager.save();
            debugManager.setEnabled(false);

            this.getLocaleManager().sendComponentMessage(context.getSender(), "command-debug-off");
            return;
        }

        debugManager.setEnabled(true);
        this.getLocaleManager().sendComponentMessage(context.getSender(), "command-debug-on");
    }

    @RoseExecutable
    public void execute(CommandContext context, String options) {
        options = options.toLowerCase();

        DebugManager debugManager = this.rosePlugin.getManager(DebugManager.class);
        if (debugManager.isEnabled()) {
            debugManager.save();
            debugManager.setEnabled(false);
            this.getLocaleManager().sendComponentMessage(context.getSender(), "command-debug-off");
        }

        debugManager.setWriteToFile(options.contains("-log"));
        debugManager.setTimerEnabled(options.contains("-timer"));
        debugManager.setDoOnce(options.contains("-doonce"));

        debugManager.setEnabled(true);
        if (options.contains("-doonce")) {
            this.getLocaleManager().sendComponentMessage(context.getSender(), "command-debug-once");
        } else {
            this.getLocaleManager().sendComponentMessage(context.getSender(), "command-debug-on");
        }
    }

}
