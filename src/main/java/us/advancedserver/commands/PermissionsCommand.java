package us.advancedserver.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import org.apache.logging.log4j.core.util.ArrayUtils;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.AdvancedServer;
import us.advancedserver.extension.Rank;
import us.advancedserver.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsCommand extends Command {

    private final Map<String, String> commands = new HashMap<String, String>() {{
        put("help", "get command list");

        put("addrank <name>", "Create a rank");

        put("delrank <rank>", "Remove rank");

        put("listranks", "List ranks");

        put("format <rank> <format>", "Change rank format");

        put("setformat <rank> <format>", "Allows you to set default chat format of a rank");

        put("setnametag <rank> <format>", "Allows you to set default nametag of a rank");

        put("listrperms <rank>", "Shows a list of all permissions from a rank");

        put("listuperms <user>", "Shows a list of all permissions from a user");

        put("setrperm <rank> <permission>", "Add a permission to a rank");

        put("delrperm <rank> <permission>", "Remove a permission from a rank");

        put("setuperm <user> <permission>", "Add a permission to an user");

        put("deluperm <user> <permission>", "Remove a permission from an user");

        put("rinfo <rank>", "Rank information");

        put("usrinfo <user>", "User information");

        put("updaterank <user> <rank>", "Update rank to an user");
    }};

    public PermissionsCommand() {
        super("permissions", "Permissions command");

        this.setAliases(new String[]{"pex", "perms"});

        Server.getInstance().getCommandMap().register(this.getName(), this);

        this.setPermissionMessage(TextFormat.RED + "You don't have permission to use this command");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(args.length <= 0) {
            sender.sendMessage(TextFormat.BLUE + "Permissions management made by iTheTrollIdk, version 1.0.2");
        } else {
            Rank rank;

            Map<String, Object> targetData;

            switch(args[0].toLowerCase()) {
                case "addrank":
                    if(!sender.hasPermission(this.getPermission() + ".addrank")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if(AdvancedServer.getInstance().getProvider().getRank(args[1]) != null) {
                        sender.sendMessage(TextFormat.RED + "Rank already exists.");
                    } else {
                        AdvancedServer.getInstance().getProvider().createOrUpdateRank(new Rank(args[1], new HashMap<>()));

                        sender.sendMessage(TextFormat.GREEN + "Rank " + args[1] + " successfully created.");
                    }
                    break;

                case "delrank":
                    if(!sender.hasPermission(this.getPermission() + ".delrank")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if(AdvancedServer.getInstance().getProvider().getRank(args[1]) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else {
                        AdvancedServer.getInstance().getProvider().deleteRank(args[1]);

                        sender.sendMessage(TextFormat.GREEN + "Rank " + args[1] + " successfully removed.");
                    }
                    break;

                case "updaterank":
                    if(!sender.hasPermission(this.getPermission() + ".updaterank")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((targetData = Utils.getTargetData(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "User doesn't exists.");
                    } else if(AdvancedServer.getInstance().getProvider().getRank(args[2]) != null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else if((rank = AdvancedServer.getInstance().getProvider().getTargetRank((String) targetData.get("username"))).getName().equals(args[2])) {
                        sender.sendMessage(TextFormat.RED + (String) targetData.get("username") + " already got this rank.");
                    } else {
                        AdvancedServer.getInstance().getProvider().setTargetRank((String) targetData.get("username"), args[2]);

                        Utils.updatePlayerPermissions((String) targetData.get("username"), true);

                        AdvancedPlayer target = (AdvancedPlayer) Server.getInstance().getPlayerExact((String) targetData.get("username"));

                        if(target != null) target.sendMessage(TextFormat.GREEN + "Your rank has been changed to a / an " + (rank.hasFormat() ? TextFormat.colorize(rank.getFormat() + "&r&a") : rank.getName()) + "!");

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " Added " + targetData.get("username") + " to the rank successfully.");
                    }

                    break;

                case "rinfo":
                    if(!sender.hasPermission(this.getPermission() + ".rinfo")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 1) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank " + args[1] + " does NOT exist.");
                    } else {
                        List<String> $inherited = new ArrayList<>();

                        rank.getInheritedRanks().forEach(($inheritedRank) -> {
                            if($inheritedRank.hasFormat()) {
                                $inherited.add(TextFormat.colorize($inheritedRank.getFormat() + "&r&a"));
                            } else {
                                $inherited.add($inheritedRank.getName());
                            }
                        });

                        sender.sendMessage(TextFormat.GREEN + "-- Rank information for " + rank.getName() + " --\n" + TextFormat.GREEN + "Default: " + (rank.isDefault() ? "true" : "false") + "\n" + TextFormat.GREEN + "Inherited ranks: " + ($inherited.isEmpty() ? "empty" : String.join(", ", $inherited)));
                    }
                break;

                case "usrinfo":
                    if(!sender.hasPermission(this.getPermission() + ".usrinfo")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 1) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((targetData = Utils.getTargetData(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "User " + args[1] + " does NOT exist.");
                    } else {
                        rank = AdvancedServer.getInstance().getProvider().getRank(args[1]);

                        sender.sendMessage(String.format(TextFormat.GREEN  + "-- User information for " + targetData.get("username") +" --\nUsername: %s\n" + TextFormat.GREEN + "Rank: %s\nPrefix: %s", rank.getOriginalDisplaynameFormat((String) targetData.get("username")), (rank.hasFormat() ? TextFormat.colorize(rank.getFormat() + "&r") : rank.getName()), "Unknown"));
                    }
                    break;

                case "listranks":
                    if(!sender.hasPermission(this.getPermission() + ".listranks")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else {
                        List<String> $result = new ArrayList<>();

                        for(Rank ranks : AdvancedServer.getInstance().getProvider().getRanks()) {
                            if(ranks.hasFormat()) {
                                $result.add(ranks.getFormat() + TextFormat.RESET + TextFormat.GREEN + " (" + ranks.getName() + ")");
                            } else {
                                $result.add(ranks.getName());
                            }
                        }

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " All registered ranks: " + String.join(", ", $result));
                    }
                    break;

                case "listuperms":
                    if(!sender.hasPermission(this.getPermission() + ".listuperms")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 1) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((targetData = AdvancedServer.getInstance().getProvider().getTargetData(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "User doesn't exists.");
                    } else {
                        List<String> permissions = AdvancedServer.getInstance().getProvider().getPlayerPermissions((String) targetData.get("username"));

                        if(permissions.isEmpty()) {
                            sender.sendMessage(TextFormat.RED + "User " + targetData.get("username") + " does not have permissions.");
                        } else {
                            sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " List of all user permissions from " + targetData.get("username") + ": " + String.join(", ", permissions));
                        }
                    }
                    break;

                case "listrperms":
                    if(!sender.hasPermission(this.getPermission() + ".listrperms")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 1) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else {
                        List<String> permissions = rank.getPermissions();

                        if(permissions.isEmpty()) {
                            sender.sendMessage(TextFormat.RED + "Rank " + rank.getName() + " doesn't have any rank permissions.");
                        } else {
                            sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " List of all group permissions from " + rank.getName() + ": " + String.join(", ", permissions));
                        }
                    }
                    break;

                case "format":
                    if(!sender.hasPermission(this.getPermission() + ".format")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else {
                        args = ArrayUtils.remove(args, 0);

                        args = ArrayUtils.remove(args, 1);

                        rank.setOriginalFormat(String.join(" ", args));

                        AdvancedServer.getInstance().getProvider().createOrUpdateRank(rank);

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " You set the format of the group to " + rank.getFormat() + ".");
                    }
                    break;

                case "setformat":
                    if(!sender.hasPermission(this.getPermission() + ".setformat")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else {
                        args = ArrayUtils.remove(args, 0);

                        args = ArrayUtils.remove(args, 1);

                        rank.setOriginalChatFormat(String.join(" ", args));

                        AdvancedServer.getInstance().getProvider().createOrUpdateRank(rank);

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " You set the chat format of rank group to " + rank.getOriginalChatFormat() + ".");
                    }
                    break;

                case "setnametag":
                    if(!sender.hasPermission(this.getPermission() + ".setnametag")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else {
                        args = ArrayUtils.remove(args, 0);

                        args = ArrayUtils.remove(args, 1);

                        rank.setOriginalNametag(String.join(" ", args));

                        AdvancedServer.getInstance().getProvider().createOrUpdateRank(rank);

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " You set the nametag of the rank to " + rank.getOriginalNametagFormat(null) + ".");
                    }
                    break;

                case "setrperm":
                    if(!sender.hasPermission(this.getPermission() + ".setrperm")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else if(!rank.addPermission(args[2])) {
                        sender.sendMessage(TextFormat.RED + "This rank already has this permission.");
                    } else {
                        AdvancedServer.getInstance().getProvider().createOrUpdateRank(rank);

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " Added permission " + args[2] + " to the rank successfully.");
                    }
                    break;

                case "setuperm":
                    if(!sender.hasPermission(this.getPermission() + ".setuperm")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((targetData = Utils.getTargetData(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "User doesn't exists.");
                    } else if(AdvancedServer.getInstance().getProvider().hasPlayerPermission((String) targetData.get("username"), args[2])) {
                        sender.sendMessage(TextFormat.RED + "User already got this permission.");
                    } else {
                        AdvancedServer.getInstance().getProvider().setPlayerPermission((String) targetData.get("username"), args[2]);

                        Utils.updatePlayerPermissions((String) targetData.get("username"));

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " Added permission " + args[2] + " to " + targetData.get("username") + " successfully.");
                    }
                    break;

                case "delrperm":
                    if(!sender.hasPermission(this.getPermission() + ".setrperm")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((rank = AdvancedServer.getInstance().getProvider().getRank(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "Rank doesn't exists.");
                    } else if(!rank.deletePermission(args[2])) {
                        sender.sendMessage(TextFormat.RED + "This rank does not have this permission");
                    } else {
                        AdvancedServer.getInstance().getProvider().createOrUpdateRank(rank);

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " Removed permission " + args[2] + " from the rank successfully");
                    }
                    break;

                case "deluperm":
                    if(!sender.hasPermission(this.getPermission() + ".deluperm")) {
                        sender.sendMessage(this.getPermissionMessage());
                    } else if(args.length <= 2) {
                        sender.sendMessage(TextFormat.RED + "Usage: " + this.getCommandUsage(s, args[0]));
                    } else if((targetData = Utils.getTargetData(args[1])) == null) {
                        sender.sendMessage(TextFormat.RED + "User doesn't exists.");
                    } else if(!AdvancedServer.getInstance().getProvider().hasPlayerPermission((String) targetData.get("username"), args[2])) {
                        sender.sendMessage(TextFormat.RED + "This player does not have this permission.");
                    } else {
                        AdvancedServer.getInstance().getProvider().deletePlayerPermission((String) targetData.get("username"), args[2]);

                        Utils.updatePlayerPermissions((String) targetData.get("username"));

                        sender.sendMessage(TextFormat.BLUE + "[Permissions]" + TextFormat.GREEN + " Removed permission " + args[2] + " from " + targetData.get("username") + " successfully.");
                    }
                    break;

                case "help":
                    sender.sendMessage(TextFormat.BLUE + "Command list:");

                    this.commands.forEach((command, description) -> sender.sendMessage(String.format("%s%s %s %s %s", TextFormat.BLUE, "/" + s, command, TextFormat.GOLD, description)));

                    break;
            }
        }
        return false;
    }

    public String getCommandUsage(String s, String argument) {
        for(int $i = 0; $i < this.commands.size(); $i++) {
            String command = this.commands.keySet().toArray(new String[]{})[$i];

            String firstArg = command.split(" ")[0];

            if(firstArg.equals(argument.toLowerCase())) return String.format("/%s %s", s, command);
        }

        return null;
    }
}