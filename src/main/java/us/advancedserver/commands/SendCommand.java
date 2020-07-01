package us.advancedserver.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import org.itxtech.synapseapi.utils.ClientData;
import us.advancedserver.AdvancedPlayer;

public class SendCommand extends Command {

    public SendCommand() {
        super("send", "Send player to another server.");

        Server.getInstance().getCommandMap().register(this.getName(), this);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        ClientData.Entry client;

        AdvancedPlayer target;

        if(args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + label + " <player> <server>");
        } else if(!(sender instanceof AdvancedPlayer)) {
            sender.sendMessage(TextFormat.RED + "Run this command in-game");
        } else if((client = ((AdvancedPlayer) sender).getSynapseEntry().getClientData().clientList.get(args[1])) == null) {
            sender.sendMessage(TextFormat.RED + "Unknown server.");
        } else if((target = (AdvancedPlayer) Server.getInstance().getPlayer(args[0])) == null) {
            sender.sendMessage("Player not found");
        } else if(target.getSynapseEntry().getServerDescription().equalsIgnoreCase(client.getDescription())) {
            sender.sendMessage(TextFormat.RED + target.getName() + " are already on this server");
        } else {
            if(target.transferByDescription(client.getDescription())) {
                sender.sendMessage(TextFormat.GREEN + "Attempting to send " + target.getName() + " to " + client.getDescription());
            } else {
                sender.sendMessage(TextFormat.RED + "Unknown error");
            }

        }
        return false;
    }
}
