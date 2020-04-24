package dev.rosewood.rosechat;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import dev.rosewood.rosechat.groups.GroupManager;
import dev.rosewood.rosechat.placeholders.PlaceholderManager;

public class RoseChat extends FloralPlugin {

    private static RoseChat instance;
    private YMLFile dataFile;
    private PlaceholderManager placeholderManager;
    private GroupManager groupManager;

    @Override
    public void onStartUp() {


        dataFile = new YMLFile("data");
        groupManager = new GroupManager();
    }

    @Override
    public void onShutDown() {
        groupManager.save();
    }

    @Override
    public void onReload() {
        instance = this;
        placeholderManager = new PlaceholderManager();
    }

    public YMLFile getDataFile() {
        return dataFile;
    }

    @Override
    public String getPluginName() {
        return "&8[&cRosechat&8]";
    }

    public static RoseChat getInstance() {
        return instance;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}
