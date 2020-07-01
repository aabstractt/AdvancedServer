package us.advancedserver.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import us.advancedserver.AdvancedServer;
import us.advancedserver.extension.BanEntry;
import us.advancedserver.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class HistoryCommand extends Command {

    public HistoryCommand() {
        super("history", "Check a player history bans");

        this.setAliases(new String[]{"hist"});

        this.setPermission("advancedserver.command.history");

        this.setPermissionMessage(TextFormat.RED + "You don't have permissions to use this command");

        Server.getInstance().getCommandMap().register(this.getName(), this);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        Map<String, Object> targetData;

        List<BanEntry> entrys;

        if(!sender.hasPermission(this.getPermission())) {
            sender.sendMessage(this.getPermissionMessage());
        } else if(args.length < 1) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + label + " <player>");
        } else if((targetData = Utils.getTargetData(args[0])) == null) {
            sender.sendMessage(TextFormat.RED + "Player not found");
        } else if((entrys = AdvancedServer.getInstance().getProvider().getAllActiveByUsername((String) targetData.get("username"))) == null) {
            sender.sendMessage(TextFormat.RED + "History from " + targetData.get("username") + " not found.");
        } else {
            StringBuilder message = new StringBuilder();

            for(BanEntry ban : entrys) {
                try {
                    message.append(String.format("&e--[&6%s ago&e]--\n", Utils.calculateRemain(System.currentTimeMillis(), (new SimpleDateFormat("dd.MM.yy HH:mm")).parse(ban.getCreatedAt()).getTime())));
                } catch(ParseException e) {
                    e.printStackTrace();
                }

                StringBuilder type = new StringBuilder(!ban.isPermanent() ? "temp-" : "");

                if(ban.getType() < 3) {
                    type.append("banned");
                } else if(ban.getType() < 5) {
                    type.append("muted");
                } else if(ban.getType() == 5) {
                    type.append("unbanned");
                } else if(ban.getType() == 6) {
                    type.append("unmuted");
                } else {
                    type.append("kicked");
                }

                if(ban.getType() < 5) {
                    message.append(String.format("&6%s&e was %s&e by &6%s&f: &e'&b%s&e' &7[%s&7]\n", targetData.get("username"), (ban.getType() < 3 ? "&4" : "&9") + type, ban.getAuthor(), ban.getReason(), ban.isFinished() ? "&3Expired" : "&6" + Utils.calculateRemain(ban.getFinishAt())));
                }
            }

            sender.sendMessage(TextFormat.colorize(message.toString()));
        }
        return true;
    }
}