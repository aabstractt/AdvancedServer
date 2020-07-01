package us.advancedserver.extension;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import com.google.common.base.Joiner;
import us.advancedserver.AdvancedServer;
import us.advancedserver.utils.Utils;

import java.util.*;

public class Rank {

    private final Map<String, Object> data;

    private final String name;

    public Rank(String name, Map<String, Object> data) {
        if(data.isEmpty()) data.putAll(new HashMap<String, Object>() {{
            put("isDefault", false);

            put("inheritance", new String[]{""});

            put("format", AdvancedServer.getInstance().getProvider().getDefaultRank().getFormat());

            put("chat_format", AdvancedServer.getInstance().getProvider().getDefaultRank().getOriginalChatFormat());

            put("nametag_format", AdvancedServer.getInstance().getProvider().getDefaultRank().getOriginalNametagFormat(null));

            put("permissions", "");
        }});

        this.name = name;

        this.data = data;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDefault() {
        return (boolean) this.data.get("isDefault");
    }

    public List<Rank> getInheritedRanks() {
        List<Rank> $inheritedRanks = new ArrayList<>();

        Rank $inheritedRank;

        if(this.data.get("inheritance").equals("") || this.data.get("inheritance") == null) return $inheritedRanks;

        for(String $rankName : (String[]) this.data.get("inheritance")) {
            if(($inheritedRank = AdvancedServer.getInstance().getProvider().getRank($rankName)) != null) $inheritedRanks.add($inheritedRank);
        }

        return $inheritedRanks;
    }

    public String getInheritedRanksString() {
        List<String> $rankNames = new ArrayList<>();

        for(Rank rank : this.getInheritedRanks()) {
            $rankNames.add(rank.getName());
        }

        return String.join(":", $rankNames);
    }

    public String getFormat() {
        return (String) this.data.get("format");
    }

    public String getOriginalChatFormat() {
        return this.getOriginalChatFormat(null, null);
    }

    public String getOriginalChatFormat(String $name, String $message) {
        if($name == null && $message == null) return (String) this.data.get("chat_format");

        List<String> $format = new ArrayList<>();

        for(Rank rank : this.getInheritedRanks()) {
            $format.add(TextFormat.colorize(rank.getFormat()));
        }

        String originalFormat = this.getOriginalChatFormat();

        assert $name != null;

        return originalFormat.replace("{inherited}", Joiner.on(" ").join($format)).replace("{prefix}", "").replace("{username}", $name).replace("{message}", $message);
    }

    public String getOriginalNametagFormat(String $name) {
        if($name == null) return (String) this.data.get("nametag_format");

        List<String> $format = new ArrayList<>();

        for(Rank rank : this.getInheritedRanks()) {
            $format.add(TextFormat.colorize(rank.getFormat()));
        }

        String originalFormat = this.getOriginalNametagFormat(null);

        return originalFormat.replace("{inherited}", Joiner.on(" ").join($format)).replace("{prefix}", "").replace("{username}", $name);
    }

    public String getOriginalDisplaynameFormat(String $name) {
        if($name == null) return (String) this.data.get("displayname_format");

        return this.getOriginalDisplaynameFormat(null).replace("{format}", TextFormat.colorize(this.getFormat())).replace("{username}", name) ;
    }

    public List<String> getPermissions() {
        List<String> $permissions = this.getPermissionsWithoutInherited();

        for(Rank rank : this.getInheritedRanks()) {
            if(!rank.getPermissions().isEmpty()) $permissions.addAll(rank.getPermissions());
        }

        return $permissions;
    }

    /**
     * @return array
     */
    public List<String> getPermissionsWithoutInherited() {
        return new ArrayList<>(Arrays.asList(((String) this.data.get("permissions")).split(":")));
    }

    public boolean addPermission(String $argument) {
        List<String> permissions = this.getPermissionsWithoutInherited();

        for(String $permission : this.getPermissions()) {
            if($permission.equals($argument)) return false;

            if($permission.equals("")) this.deletePermission($permission);
        }

        permissions.add($argument);

        this.data.put("permissions", String.join(":", permissions));

        return true;
    }

    public boolean deletePermission(String $argument) {
        List<String> permissions = this.getPermissionsWithoutInherited();

        for(int $i = 0; $i < permissions.size(); $i++) {
            if(permissions.get($i).equals($argument)) {
                permissions.remove($i);

                this.data.put("permissions", String.join(":", permissions));

                return true;
            }
        }

        return false;
    }

    public void setOriginalFormat(String $format) {
        this.data.put("format", $format);
    }

    public void setOriginalChatFormat(String $chatFormat) {
        this.data.put("chat_format", $chatFormat);
    }

    public void setOriginalNametag(String $nametagFormat) {
        this.data.put("nametag_format", $nametagFormat);
    }

    public boolean hasFormat() {
        return this.data.get("format") != null;
    }

    public void updatePlayersPermissions() {
        for(Player player : Server.getInstance().getOnlinePlayers().values()) {
            if(AdvancedServer.getInstance().getProvider().getTargetRank(player.getName()).getName().equals(this.getName())) {
                Utils.updatePlayerPermissions(player.getName(), true);
            }
        }
    }
}