package us.advancedserver.utils;

import cn.nukkit.Server;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.utils.TextFormat;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.AdvancedServer;
import us.advancedserver.extension.Rank;
import us.advancedserver.provider.MysqlProvider;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final Map<String, PermissionAttachment> attachment = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static MysqlProvider loadProvider() throws SQLException {
        String typeProvider = AdvancedServer.getInstance().getConfig().getString("provider").toLowerCase();

        if(! typeProvider.equals("mysql") && ! typeProvider.equals("yaml")) {
            AdvancedServer.getInstance().getLogger().error("Invalid provider");

            AdvancedServer.getInstance().setEnabled(false);

            return null;
        }

        return new MysqlProvider((Map<String, Object>) AdvancedServer.getInstance().getConfig().get("mysql"), (Map<String, Object>) AdvancedServer.getInstance().getConfig().get("ranks"));
    }

    public static Map<String, Object> getTargetData(String username) {
        AdvancedPlayer target = (AdvancedPlayer) Server.getInstance().getPlayer(username);

        if(target != null) {
            username = target.getName();
        }

        return AdvancedServer.getInstance().getProvider().getTargetData(username);
    }

    public static PermissionAttachment getAttachment(String username) {
        if(! attachment.containsKey(username.toLowerCase()))
            attachment.put(username.toLowerCase(), Server.getInstance().getPlayerExact(username).addAttachment(AdvancedServer.getInstance()));

        return attachment.get(username.toLowerCase());
    }

    public static void removeFromAttachment(String username) {
        attachment.remove(username.toLowerCase());
    }

    public static void updatePlayerPermissions(String username) {
        updatePlayerPermissions(username, false);
    }

    public static void updatePlayerPermissions(String username, boolean $updateTag) {
        AdvancedPlayer player = (AdvancedPlayer) Server.getInstance().getPlayerExact(username);

        if(player == null) return;

        List<String> $permissions = AdvancedServer.getInstance().getProvider().getPlayerPermissions(username);

        Rank rank = AdvancedServer.getInstance().getProvider().getTargetRank(username);

        if(rank == null) {
            rank = AdvancedServer.getInstance().getProvider().getDefaultRank();

            AdvancedServer.getInstance().getProvider().setTargetRank(username, rank.getName());
        }

        if($updateTag) player.setNameTag(rank.getOriginalNametagFormat(username));

        player.setDisplayName(player.getName());

        $permissions.addAll(rank.getPermissions());

        PermissionAttachment $attachment = getAttachment(username);

        $attachment.clearPermissions();

        for(String permission : $permissions) {
            $attachment.setPermission(permission, true);
        }
    }

    public static void createPlayer(Map<String, Object> data) {
        AdvancedServer.getInstance().getProvider().setTargetData(data);
    }

    public static String calculateRemain(Long finishAt) {
        return calculateRemain(finishAt, System.currentTimeMillis());
    }

    public static String calculateRemain(Long finishAt, Long actualAt) {
        int finishAtSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(finishAt);

        int actualAtSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(actualAt);

        int diff = finishAtSeconds - actualAtSeconds;

        String timeRemaining;

        if(diff >= (60 * 60 * 24)) {
            timeRemaining = (diff / 86400) + " days, " + (diff % 86400) / 3600 + " hours, " + ((diff % 86400) % 3600) / 60 + " minutes";
        } else if(diff >= 3600) {
            int hours = diff / 3600;

            timeRemaining = hours + " hours, " + ((diff - (hours * 3600)) / 60) + " minutes";
        } else if(diff >= 60) {
            timeRemaining = (diff / 60) + " minutes";
        } else {
            timeRemaining = diff + " seconds";
        }

        return timeRemaining;
    }

    public static Long calculateTime(String arguments) {
        Object[] $characters = arguments.replaceAll("[0-9]", "").split("");

        int $currentInteger = Integer.parseInt(arguments.replaceAll("[a-z]", ""));

        TimeUnit tu = null;

        for(Object $character : $characters) {
            if($character instanceof String) {
                String $k = $character.toString().toLowerCase();

                switch($k) {
                    case "s":
                        tu = TimeUnit.SECONDS;
                        break;
                    case "m":
                        tu = TimeUnit.MINUTES;
                        break;
                    case "h":
                        tu = TimeUnit.HOURS;
                        break;
                    case "d":
                        tu = TimeUnit.DAYS;
                        break;
                }
            }
        }

        if(tu == null || $currentInteger <= 0) {
            return null;
        }

        return System.currentTimeMillis() + tu.toMillis($currentInteger);
    }

    public static void giveItemsLobby(AdvancedPlayer player) {
        player.setDisplayName(player.getName());

        player.setAllowFlight(false);

        player.getInventory().clearAll();

        player.getInventory().setItem(0, (Item.get(Item.COMPASS)).setCustomName(TextFormat.colorize("&r&4&lGame Selector &r&7(Right click)")));

        player.getInventory().setItem(1, (Item.get(Item.RED_FLOWER)).setCustomName(TextFormat.colorize("&r&9&lLoot &r&7(Right click)")));

        if(player.hasPermission("spymode")) {
            player.sendMessage(TextFormat.colorize("&9&o" + player.getName() + "&r&7 joined &fHub"));
        }

        if(player.getCurrentlyLoot() != null && (!player.getCurrentlyLoot().equalsIgnoreCase("Particles") && !player.getCurrentlyLoot().equalsIgnoreCase("Size"))) {
            giveCurrentlyLoot(player, player.getCurrentlyLoot());
        } else if(player.getCurrentlyLoot() != null && player.getCurrentlyLoot().equalsIgnoreCase("Size")) {
            player.setScale(player.getCurrentlySize());
        }

        StringBuilder text = new StringBuilder("&e&lPlaying on &9PLAY.CUBEHERO.US");
        
        if(!player.isRegistered()) {
            text.append("\n&aUse /register <password> <password>");

            player.sendMessage(TextFormat.YELLOW + "This account is not registered, for better experience you must register using /register <password> <password>");
        } else if(!player.isAuthenticated()) {
            text.append("\n&aUse /login <password>");

            player.sendMessage(TextFormat.YELLOW + "This account is registered, for better experience you must authenticate using /login <password>");
        }

        if(player.getLastBossId() != -1) player.removeBossBar(player.getLastBossId());

        player.createBossBar(TextFormat.colorize(text.toString()), 100);
        
        updatePlayerPermissions(player.getName(), true);
    }

    public static void giveCurrentlyLoot(AdvancedPlayer player, String currentlyLoot) {
        Item item = null;

        if(currentlyLoot.equalsIgnoreCase("Snow Golem")) {
            item = (Item.get(Item.SNOWBALL)).setCustomName(TextFormat.colorize("&r&b&lGolem Launch &r&7(Right click)"));
        } else if(currentlyLoot.equalsIgnoreCase("Sheep Explode")) {
            item = (Item.get(Item.EGG)).setCustomName(TextFormat.colorize("&5&c&lSheep Explode &r&7(Right click)"));
        } else if(currentlyLoot.equalsIgnoreCase("Paintball")) {
            item = (Item.get(Item.ENDER_PEARL)).setCustomName(TextFormat.colorize("&r&9&lPaintball &r&7(Right click)"));
        } else if(currentlyLoot.equalsIgnoreCase("Launch")) {
            item = (Item.get(288)).setCustomName(TextFormat.colorize("&r&d&lLaunch &r&7(Right click)"));
        }

        if(item != null) {
            player.getInventory().setItem(4, item);

            player.setCurrentlyLoot(currentlyLoot);
        } else if(currentlyLoot.equalsIgnoreCase("Particles")) {
            FormWindowSimple form = new FormWindowSimple(TextFormat.colorize("&9&Particles Menu"), "Select a particle");

            for(String button : new String[]{"Flame", "HappyVillager", "Enchant", "Heart", "Music", "Slime", "Smoke", "Potion", "Ice"}) {
                form.addButton(new ElementButton(TextFormat.colorize("&5" + button), button));
            }

            player.showFormWindow(form, 39483);
        } else if(currentlyLoot.equalsIgnoreCase("Size")) {
            FormWindowCustom form = new FormWindowCustom(TextFormat.colorize("&5&lSize menu"));

            form.addElement(new ElementStepSlider("Choose a size", Arrays.asList("0.5", "0.7", "1", "1.4", "2")));

            player.showFormWindow(form, 47845);
        }
    }
}