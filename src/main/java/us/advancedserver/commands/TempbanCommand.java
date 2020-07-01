package us.advancedserver.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapseEntry;
import us.advancedserver.AdvancedServer;
import us.advancedserver.extension.BanEntry;
import us.advancedserver.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class TempbanCommand extends Command {

    public TempbanCommand() {
        super("tempban", "Ban a player temporary");

        this.setAliases(new String[]{"tban"});

        this.setPermission("advancedserver.command.tempban");

        this.setPermissionMessage(TextFormat.RED + "You don't have permissions to use this command");

        Server.getInstance().getCommandMap().register(this.getName(), this);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Map<String, Object> targetData;

        Long time;

        if(args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + label + " <player> <format> <reason>");
        } else if(!sender.hasPermission(this.getPermission())) {
            sender.sendMessage(this.getPermissionMessage());
        } else if((targetData = Utils.getTargetData(args[0])) == null) {
            sender.sendMessage(TextFormat.RED + String.format("%s not found", args[0]));
        } else if((time = Utils.calculateTime(args[1])) == null) {
            sender.sendMessage(TextFormat.RED + "Please enter a valid time format.");
        } else {
            BanEntry ban = AdvancedServer.getInstance().getProvider().getBanActiveByUsername((String) targetData.get("username"));

            if(ban != null) {
                ban.delete();
            }

            for(SynapseEntry entry : SynapseAPI.getInstance().getSynapseEntries().values()) {
                entry.sendPluginMessage(AdvancedServer.getInstance(), "actions", String.format("%s:%s:%s:PLAYER_TEMPBANNED", targetData.get("username"), Utils.calculateRemain(time), sender.getName()).getBytes());

                break;
            }

            (new BanEntry(2, (String) targetData.get("username"), (new SimpleDateFormat("dd.MM.yy HH:mm")).format(new Date()), time, sender.getName(), args.length > 2 ? args[2] : "Unknown", (String) targetData.get("lastAddress"))).submit();
        }

        return false;
    }
}