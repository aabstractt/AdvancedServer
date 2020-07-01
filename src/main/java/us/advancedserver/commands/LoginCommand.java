package us.advancedserver.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class LoginCommand extends Command {

    public LoginCommand() {
        super("login", "Login to your account", "/login <password>", new String[]{"l", "auth"});

        Server.getInstance().getCommandMap().register(this.getName(), this);
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!(sender instanceof AdvancedPlayer)) {
            sender.sendMessage(TextFormat.RED + "Run this command in-game");
        } else if(args.length < 1) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + s + " <password>");
        } else if(((AdvancedPlayer) sender).isRegistered()) {
            sender.sendMessage(TextFormat.YELLOW + "This account is not registered, for better experience you must register using /register <password> <password>");
        } else if(((AdvancedPlayer) sender).isAuthenticated()) {
            sender.sendMessage(TextFormat.RED + "You're already logged in!");
        } else if(!((AdvancedPlayer) sender).containsMyPassword(args[0])) {
            sender.sendMessage(TextFormat.RED + "Incorrect password! Please try again.");
        } else {
            Utils.createPlayer(new HashMap<String, Object>() {{
                put("username", sender.getName());

                put("address", ((AdvancedPlayer) sender).getAddress());

                put("lastAddress", ((AdvancedPlayer) sender).getAddress());

                put("auth_date", (new SimpleDateFormat("dd.MM.yy HH:mm")).format(new Date()));
            }});

            ((AdvancedPlayer) sender).setAuthenticated(true);

            sender.sendMessage(TextFormat.GREEN + "You are now logged in, we hope you enjoy your stay!");

            Utils.giveItemsLobby((AdvancedPlayer) sender);
        }
        return false;
    }
}
