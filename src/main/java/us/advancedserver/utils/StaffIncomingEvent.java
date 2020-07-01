package us.advancedserver.utils;

import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import org.itxtech.synapseapi.SynapseEntry;
import org.itxtech.synapseapi.messaging.PluginMessageListener;

public class StaffIncomingEvent implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(SynapseEntry synapseEntry, String s, byte[] bytes) {
        String[] data = new String(bytes).split(":");

        if(data[0].equalsIgnoreCase("CONSOLE")) {
            Server.getInstance().getLogger().info(TextFormat.colorize(data[1]));
        }
    }
}