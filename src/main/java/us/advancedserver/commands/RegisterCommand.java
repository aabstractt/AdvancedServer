package us.advancedserver.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.AdvancedServer;
import us.advancedserver.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class RegisterCommand extends Command {

    public RegisterCommand() {
        super("register", "Register command");

        Server.getInstance().getCommandMap().register(this.getName(), this);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!(sender instanceof AdvancedPlayer)) {
            sender.sendMessage(TextFormat.RED + "Run this command in-game");
        } else if(((AdvancedPlayer) sender).isRegistered()) {
            sender.sendMessage(TextFormat.RED + "Uh oh, looks like your account is already registered! " + TextFormat.GOLD + "Please change your username and try again.");
        } else if(args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + label + " <password> <password>");
        } else if(args[0].length() < 4) {
            sender.sendMessage(TextFormat.RED + "Please choose a longer password.");
        } else if(!args[0].equalsIgnoreCase(args[1])) {
            sender.sendMessage(TextFormat.RED + "Sorry but the passwords you entered don't match!");
        } else {
            Utils.createPlayer(new HashMap<String, Object>() {{
                put("username", sender.getName());

                put("address", ((AdvancedPlayer) sender).getAddress());

                put("lastAddress", ((AdvancedPlayer) sender).getAddress());

                put("auth_date", (new SimpleDateFormat("dd.MM.yy HH:mm")).format(new Date()));
            }});

            AdvancedServer.getInstance().getProvider().setTargetAuth(new HashMap<String, String>() {{
                put("username", sender.getName());

                put("password", args[0]);

                put("auth_date", (new SimpleDateFormat("dd.MM.yy HH:mm")).format(new Date()));
            }});

            AdvancedServer.getInstance().getProvider().setTargetRank(sender.getName(), AdvancedServer.getInstance().getProvider().getDefaultRank().getName());

            sender.sendMessage(TextFormat.GREEN + "You have successfully registered, have fun!");

            Utils.giveItemsLobby((AdvancedPlayer) sender);
        }
        return false;
    }
}
