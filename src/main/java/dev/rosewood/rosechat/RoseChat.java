package dev.rosewood.rosechat;

import dev.rosewood.rosechat.commands.*;
import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.command.CommandManager;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;

public class RoseChat extends FloralPlugin {

    private static RoseChat instance;
    private YMLFile dataFile;

    @Override
    public void onStartUp() {
        new RosechatCommandManager("rosechat", "rosechat help")
                .addCommandManager(new CommandManager(new CommandMessage()))
                .addCommandManager(new CommandManager(new CommandReply()))
                .addCommandManager(new CommandManager(new CommandStaffChat()))
                .addCommandManager(new CommandManager(new CommandBroadcast()))
                .addCommandManager(new CommandManager(new CommandToggleSound()))
                .addCommandManager(new CommandManager(new CommandSpy()))
                .addCommandManager(new CommandManager(new CommandToggleMessage()))
                .addCommandManager(new CommandManager(new CommandChannel()))
                .addCommandManager(new CommandManager(new CommandGroup()));

        dataFile = new YMLFile("data");
    }

    @Override
    public void onShutDown() {

    }

    @Override
    public void onReload() {

    }

    @Override
    public String getPluginName() {
        return "&8[&cRosechat&8]";
    }

    public static RoseChat getInstance() {
        return instance;
    }
}
