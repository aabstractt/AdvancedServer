package us.advancedserver;

import cn.nukkit.plugin.PluginBase;
import org.itxtech.synapseapi.SynapseAPI;
import us.advancedserver.commands.*;
import us.advancedserver.listener.EventListener;
import us.advancedserver.provider.Provider;
import us.advancedserver.utils.StaffIncomingEvent;
import us.advancedserver.utils.Utils;

import java.sql.SQLException;

public class AdvancedServer extends PluginBase {

    private static AdvancedServer instance;

    private Provider provider;

    private double START_TIME;

    public void onLoad() {
        START_TIME = System.currentTimeMillis();

        instance = this;

        getLogger().info("Starting AdvancedServer integration...");

        this.saveDefaultConfig();

        try {
            this.provider = Utils.loadProvider();
        } catch(SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("Setting up a channels...");

        SynapseAPI.getInstance().getMessenger().registerOutgoingPluginChannel(this, "staff:channel");

        SynapseAPI.getInstance().getMessenger().registerOutgoingPluginChannel(this, "actions");

        SynapseAPI.getInstance().getMessenger().registerIncomingPluginChannel(this, "actions", new StaffIncomingEvent());

        getLogger().info("Setting up a events...");

        new Events();

        new EventListener();

        getLogger().info("Setting up a commands...");

        new PermissionsCommand();

        new LoginCommand();

        new RegisterCommand();

        new SendCommand();

        new TempbanCommand();

        new HistoryCommand();
        
        getLogger().info("Done (" + ((System.currentTimeMillis() - START_TIME) / 1000.0D) + ")!");

    }

    public Provider getProvider() {
        return this.provider;
    }

    public static AdvancedServer getInstance() {
        return instance;
    }
}