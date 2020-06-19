package us.advancedserver;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.utils.ClientChainData;
import us.advancedserver.utils.Utils;

import java.util.HashMap;

public class AdvancedPlayer extends Player {

    private String realName;

    private HashMap<String, Object> authData;

    private boolean authenticated = false;

    public AdvancedPlayer(SourceInterface interfaz, Long clientID, String ip, int port) {
        super(interfaz, clientID, ip, port);
    }

    public String getRealName() {
        return this.realName;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void handleAuthenticated(String password) {
        if(this.authData.get("password").equals(password)) {
            this.authenticated = true;
        }
    }

    public void handleDataPacket(DataPacket pk) {
        if(pk instanceof LoginPacket) {
            this.realName = ((LoginPacket) pk).username;

            this.authData = AdvancedServer.getInstance().getProvider().handleAuthData(this.getRealName());

            if(!ClientChainData.read((LoginPacket) pk).isXboxAuthed()) {
                String guestName = Utils.getGuestName();

                while(Utils.getPlayerByGuestName(guestName) != null) {
                    guestName = Utils.getGuestName();
                }

                ((LoginPacket) pk).username = guestName;

                Utils.createPlayer(new HashMap<String, Object>() {{
                    put("username", getRealName());

                    put("guestName", ((LoginPacket) pk).username);

                    put("address", getAddress());

                    put("auth_date", "xd");
                }}, true);
            } else {
                Utils.createPlayer(new HashMap<String, Object>() {{
                    put("username", getRealName());

                    put("address", getAddress());

                    put("lastAddress", getAddress());

                    put("auth_date", "a");
                }}, false);
            }
        }

        super.handleDataPacket(pk);
    }
}