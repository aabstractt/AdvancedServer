package us.advancedserver;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.*;
import cn.nukkit.event.level.ThunderChangeEvent;
import cn.nukkit.event.level.WeatherChangeEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.utils.TextFormat;
import org.itxtech.synapseapi.event.player.SynapsePlayerCreationEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTransferEvent;
import us.advancedserver.extension.BanEntry;
import us.advancedserver.task.CheckMessageAsync;
import us.advancedserver.utils.Utils;

public class Events implements Listener {

    public Events() {
        Server.getInstance().getPluginManager().registerEvents(this, AdvancedServer.getInstance());
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent ev) {
        if(ev.toWeatherState()) ev.setCancelled();
    }

    @EventHandler
    public void onThunder(ThunderChangeEvent ev) {
        if(ev.toThunderState()) ev.setCancelled();
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent ev) {
        ev.setCancelled();
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent ev) {
        if(ev.getBlock().getLevel() == Server.getInstance().getDefaultLevel()) {
            ev.setCancelled();
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent ev) {
        ev.setCancelled();
    }

    @EventHandler
    public void onFade(BlockFadeEvent ev) {
        if(ev.getBlock().getLevel() == Server.getInstance().getDefaultLevel()) {
            ev.setCancelled();
        }
    }

    @EventHandler
    public void onFall(BlockFallEvent ev) {
        if(ev.getBlock().getLevel() == Server.getInstance().getDefaultLevel()) {
            ev.setCancelled();
        }
    }

    @EventHandler
    public void onUpdate(BlockUpdateEvent ev) {
        ev.setCancelled();
    }

    @EventHandler
    public void onCreation(SynapsePlayerCreationEvent ev) {
        ev.setPlayerClass(AdvancedPlayer.class);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        AdvancedPlayer player = (AdvancedPlayer) ev.getPlayer();

        Utils.removeFromAttachment(player.getName());
    }

    @EventHandler
    public void onPlayerTransfer(SynapsePlayerTransferEvent ev) {
        AdvancedPlayer player = (AdvancedPlayer) ev.getPlayer();

        player.getSynapseEntry().sendPluginMessage(AdvancedServer.getInstance(), "staff:channel", String.format("%s:%s:%s:CHANGE_SERVER", player.getName(), player.getSynapseEntry().getServerDescription(), ev.getClientData().getDescription()).getBytes());
    }

    @EventHandler
    public void onChat(PlayerChatEvent ev) {
        AdvancedPlayer player = (AdvancedPlayer) ev.getPlayer();

        BanEntry entry = AdvancedServer.getInstance().getProvider().getMuteActiveByUsername(player.getName());

        if(entry != null) {
            if(entry.isFinished()) {
                try {
                    entry.delete("CONSOLE", true);

                    entry = null;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Server.getInstance().getScheduler().scheduleAsyncTask(new CheckMessageAsync(player.getName(), TextFormat.clean(ev.getMessage()), player.lastMessageTime, entry, AdvancedServer.getInstance().getProvider().getTargetRank(player.getName())));

        ev.setCancelled();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        AdvancedPlayer player = (AdvancedPlayer) ev.getPlayer();

        Utils.giveItemsLobby(player);

        ev.setJoinMessage("");

        player.getSynapseEntry().sendPluginMessage(AdvancedServer.getInstance(), "staff:channel", (player.getName() + ":STAFF_JOIN").getBytes());

        player.getSynapseEntry().sendPluginMessage(AdvancedServer.getInstance(), "staff:channel", String.format("%s:%s:%s:CHANGE_SERVER", player.getName(), player.getSynapseEntry().getServerDescription(), player.getSynapseEntry().getServerDescription()).getBytes());
    }
}