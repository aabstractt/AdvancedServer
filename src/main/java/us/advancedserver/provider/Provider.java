package us.advancedserver.provider;

import cn.nukkit.utils.Hash;
import us.advancedserver.extension.Rank;

import java.util.HashMap;
import java.util.List;

public interface Provider {

    String getName();

    void setTargetData(HashMap<String, Object> data);

    void setOfflineData(HashMap<String, Object> data);

    HashMap<String, Object> getTargetData(String username);

    HashMap<String, Object> getOfflineData(String username);

    void deleteTargetOffline(String guestName);

    void deleteTargetOffline(String columnName, String guestName);

    void createOrUpdateRank(Rank rank);

    void deleteRank(String rankName);

    Rank getRank(String rankName);

    List<Rank> getRanks();

    Rank getDefaultRank();

    void setTargetRank(String username, String rankName);

    Rank getTargetRank(String name);

    void setPlayerPermission(String username, String permission);

    List<String> getPlayerPermissions(String username);

    boolean hasPlayerPermission(String username, String permission);

    void deletePlayerPermission(String username, String permission);

    HashMap<String, Object> handleAuthData(String username);
}
