package us.advancedserver.provider;

import us.advancedserver.extension.BanEntry;
import us.advancedserver.extension.Rank;

import java.util.List;
import java.util.Map;

public interface Provider {

    String getName();

    void setTargetData(Map<String, Object> data);

    void setTargetAuth(Map<String, String> data);

    Map<String, Object> getTargetData(String username);

    Map<String, Object> getTargetAuth(String username);

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

    void addBan(BanEntry entry);

    BanEntry getBanActiveByUsername(String username);

    BanEntry getBanActiveByUsername(String username, boolean permanent);

    BanEntry getMuteActiveByUsername(String username);

    BanEntry getMuteActiveByUsername(String username, boolean permanent);

    List<BanEntry> getAllDeleteByUsername(String username);

    List<BanEntry> getAllActiveByUsername(String username);

    void deleteEntry(BanEntry entry);

    void updateEntry(BanEntry entry);
}