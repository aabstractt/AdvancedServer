package us.advancedserver.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.permission.PermissionAttachment;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.AdvancedServer;
import us.advancedserver.extension.Rank;
import us.advancedserver.provider.MysqlProvider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Utils {

    private static final Map<String, PermissionAttachment> attachment = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static MysqlProvider loadProvider() throws SQLException {
        String typeProvider = AdvancedServer.getInstance().getConfig().getString("provider").toLowerCase();

        if(!typeProvider.equals("mysql") && !typeProvider.equals("yaml")) {
            AdvancedServer.getInstance().getLogger().error("Invalid provider");

            AdvancedServer.getInstance().setEnabled(false);

            return null;
        }

        return new MysqlProvider((Map<String, Object>) AdvancedServer.getInstance().getConfig().get("mysql"), (Map<String, Object>) AdvancedServer.getInstance().getConfig().get("ranks"));
    }

    public static String getGuestName() {
        String[] $ks = "abcdefghijklmnopqrstuvwxyz".split("");

        Random rand = new Random();

        return "guest" + rand.nextInt() + $ks[rand.nextInt($ks.length - 1)] + "0a";
    }

    public static AdvancedPlayer getPlayerByGuestName(String guestName) {
        for(Player player : Server.getInstance().getOnlinePlayers().values()) {
            if(player.getName().equalsIgnoreCase(guestName)) {
                return (AdvancedPlayer) player;
            }
        }

        return null;
    }

    public static Map<String, Object> getTargetData(String username) {
        AdvancedPlayer target = (AdvancedPlayer) Server.getInstance().getPlayer(username);

        if(target != null && (target.isAuthenticated())) {
            username = target.getRealName();
        }

        return AdvancedServer.getInstance().getProvider().getTargetData(username);
    }

    public static PermissionAttachment getAttachment(String username) {
        if(!attachment.containsKey(username.toLowerCase())) attachment.put(username.toLowerCase(), Server.getInstance().getPlayerExact(username).addAttachment(AdvancedServer.getInstance()));

        return attachment.get(username.toLowerCase());
    }

    public static void updatePlayerPermissions(String username) {
        updatePlayerPermissions(username, false);
    }

    public static void updatePlayerPermissions(String username, boolean $updateTag) {
        AdvancedPlayer player = (AdvancedPlayer) Server.getInstance().getPlayerExact(username);

        if(player == null) return;

        List<String> $permissions = AdvancedServer.getInstance().getProvider().getPlayerPermissions(username);

        Rank rank = AdvancedServer.getInstance().getProvider().getTargetRank(username);

        if($updateTag) player.setNameTag(rank.getOriginalNametagFormat(username));

        $permissions.addAll(rank.getPermissions());

        PermissionAttachment $attachment = getAttachment(username);

        $attachment.clearPermissions();

        for(String permission : $permissions) {
            $attachment.setPermission(permission, true);
        }
    }

    public static void createPlayer(HashMap<String, Object> data, boolean offline) {
        if(offline) {
            AdvancedServer.getInstance().getProvider().setOfflineData(data);
        } else {
            AdvancedServer.getInstance().getProvider().setTargetData(data);
        }
    }
}