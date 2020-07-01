package us.advancedserver.extension;

import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapseEntry;
import us.advancedserver.AdvancedServer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BanEntry {

    private final Integer type;

    private final String name;

    private final String createdAt;

    private final long finishAt;

    private final String author;

    private final String reason;

    private final String address;

    private boolean finished;

    private final int id;

    public BanEntry(Integer type, String name, String createdAt, long finishAt, String author, String reason, String address) {
        this(type, name, createdAt, finishAt, author, reason, address, false, 0);
    }

    public BanEntry(Integer type, String name, String createdAt, long finishAt, String author, String reason, String address, boolean finished, int id) {
        this.type = type;

        this.name = name;

        this.createdAt = createdAt;

        this.finishAt = finishAt;

        this.author = author;

        this.reason = reason;

        this.address = address;

        this.finished = finished;

        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public long getFinishAt() {
        return this.finishAt;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getReason() {
        return this.reason;
    }

    public String getAddress() {
        return this.address;
    }

    public int getId() {
        return this.id;
    }

    public Integer getType() {
        return this.type;
    }

    public boolean isFinished() {
        if(!this.finished) {
            if(System.currentTimeMillis() > this.getFinishAt()) this.finished = true;
        }

        return this.finished;
    }

    public boolean isPermanent() {
        return this.type == 1 || this.type == 3;
    }

    public void delete() {
        try {
            this.delete(null, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void submit() {
        for(SynapseEntry entry : SynapseAPI.getInstance().getSynapseEntries().values()) {
            entry.sendPluginMessage(AdvancedServer.getInstance(), "actions", String.format("ADD_BAN:%s:%s:%s:%s:%s:%s", this.getName(), this.author, this.getAddress(), this.reason, this.type, this.finishAt).getBytes());
        }

        AdvancedServer.getInstance().getProvider().addBan(this);
    }

    public void delete(String sender, boolean value) throws Exception {
        for(SynapseEntry entry : SynapseAPI.getInstance().getSynapseEntries().values()) {
            entry.sendPluginMessage(AdvancedServer.getInstance(), "actions", String.format("REMOVE_BAN:%s:%s:%s:%s", this.getName(), this.getAddress(), this.type, sender).getBytes());
        }

        if(value) {
            AdvancedServer.getInstance().getProvider().deleteEntry(this);

            if(sender != null) {
                AdvancedServer.getInstance().getProvider().addBan(new BanEntry(this.type < 3 ? 5 : 6, this.name, (new SimpleDateFormat("dd.MM.yy HH:mm")).format(new Date()), this.finishAt, sender, this.reason, this.address));
            }
        } else if(AdvancedServer.getInstance().getProvider().getAllActiveByUsername(this.name) == null) {
            throw new Exception("Ban with id " + this.id + " not found.");
        } else {
            AdvancedServer.getInstance().getProvider().updateEntry(this);
        }
    }
}