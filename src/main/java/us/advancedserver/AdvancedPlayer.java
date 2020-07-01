package us.advancedserver;

import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.utils.ClientChainData;
import cn.nukkit.utils.DummyBossBar;
import org.itxtech.synapseapi.SynapseEntry;
import org.itxtech.synapseapi.SynapsePlayer;
import us.advancedserver.utils.Utils;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdvancedPlayer extends SynapsePlayer {

    private long lastBossId;

    private String lastBossText;

    private Map<String, Object> authData;

    private boolean authenticated = false;

    private String currentlyLoot = null;

    private String currentlyParticle = null;

    private float currentlySize = 0;

    public Long lastMessageTime;

    public AdvancedPlayer(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress address) {
        super(interfaz, synapseEntry, clientID, address);
    }

    public boolean isOp() {
        return this.isAuthenticated() && super.isOp();
    }

    public boolean isRegistered() {
        return this.authData != null || this.getLoginChainData().isXboxAuthed();
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public void setAuthenticated(boolean value) {
        this.authenticated = value;
    }

    public long getLastBossId() {
        return this.lastBossId;
    }

    public String getLastBossText() {
        return this.lastBossText;
    }

    public boolean containsMyPassword(String password) {
        return this.authData.get("password").equals(password);
    }

    public long createBossBar(DummyBossBar dummyBossBar) {
        this.lastBossId = dummyBossBar.getBossBarId();

        this.lastBossText = dummyBossBar.getText();

        return dummyBossBar.getBossBarId();
    }

    public void setCurrentlyLoot(String loot) {
        this.setCurrentlyLoot(loot, this.getCurrentlyParticle());
    }

    public void setCurrentlyLoot(String loot, String currentlyParticle) {
        this.setCurrentlyLoot(loot, currentlyParticle, this.getCurrentlySize());
    }

    public void setCurrentlyLoot(String loot, float currentlySize) {
        this.setCurrentlyLoot(loot, this.getCurrentlyParticle(), currentlySize);
    }

    public void setCurrentlyLoot(String loot, String currentlyParticle, float currentlySize) {
        this.currentlyLoot = loot;

        this.currentlyParticle = currentlyParticle;

        this.currentlySize = currentlySize;
    }

    public String getCurrentlyLoot() {
        return this.currentlyLoot;
    }

    public String getCurrentlyParticle() {
        return this.currentlyParticle;
    }

    public float getCurrentlySize() {
        return this.currentlySize;
    }

    public void handleDataPacket(DataPacket pk) {
        if(pk instanceof LoginPacket) {
            this.authData = AdvancedServer.getInstance().getProvider().getTargetAuth(this.getName());

            if((this.authData == null && AdvancedServer.getInstance().getProvider().getTargetData(this.getName()) != null) && !ClientChainData.read((LoginPacket) pk).isXboxAuthed()) {
                this.close("You cannot use this account");
            } else if(ClientChainData.read((LoginPacket) pk).isXboxAuthed()) {
                Utils.createPlayer(new HashMap<String, Object>() {{
                    put("username", getName());

                    put("address", getAddress());

                    put("lastAddress", getAddress());

                    put("auth_date", (new SimpleDateFormat("dd.MM.yy HH:mm")).format(new Date()));
                }});

                Utils.giveItemsLobby(this);
            }
        }

        super.handleDataPacket(pk);
    }
}