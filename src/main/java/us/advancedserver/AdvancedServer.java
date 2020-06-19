package us.advancedserver;

import cn.nukkit.plugin.PluginBase;
import us.advancedserver.commands.PermissionsCommand;
import us.advancedserver.provider.Provider;
import us.advancedserver.utils.Utils;

import java.sql.SQLException;

public class AdvancedServer extends PluginBase {

    private static AdvancedServer instance;

    private Provider provider;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Starting AdvancedServer integration...");

        this.saveDefaultConfig();

        try {
            this.provider = Utils.loadProvider();
        } catch(SQLException exception) {
            exception.printStackTrace();
        }

        getLogger().info("Setting up a commands...");

        new PermissionsCommand();

        getLogger().info("Done!");
    }

    public Provider getProvider() {
        return this.provider;
    }

    public static AdvancedServer getInstance() {
        return instance;
    }
}
