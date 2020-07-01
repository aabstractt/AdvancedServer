package us.advancedserver.task;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.extension.BanEntry;
import us.advancedserver.extension.Rank;
import us.advancedserver.utils.Utils;

public class CheckMessageAsync extends AsyncTask {

    private static final Integer SUCCESS = 0, CHAT_COOLDOWN = 1, ALREADY_MUTED = 2;

    private final String username;

    private final String message;

    private final long lastMessageTime;

    private final BanEntry entry;

    private final Rank rank;

    public CheckMessageAsync(String username, String message, long lastMessageTime, BanEntry entry, Rank rank) {
        this.username = username;

        this.message = message;

        this.lastMessageTime = lastMessageTime;

        this.entry = entry;

        this.rank = rank;
    }

    @Override
    public void onRun() {
        if(System.currentTimeMillis()  - this.lastMessageTime <= 3000) {
            this.setResult(CHAT_COOLDOWN);
        } else if(this.entry != null) {
            this.setResult(ALREADY_MUTED);
        } else {
            this.setResult(SUCCESS);
        }
    }

    @Override
    public void onCompletion(Server server) {
        AdvancedPlayer player = (AdvancedPlayer) server.getPlayerExact(this.username);

        if(player == null) {
            server.getLogger().error(this.username + " not is online.");
        } else {
            if(player.hasPermission("advancedserver.antiflood")) this.setResult(SUCCESS);

            if(this.getResult() == SUCCESS) {
                server.broadcastMessage(this.rank.getOriginalChatFormat(this.username, this.message));

                player.lastMessageTime = System.currentTimeMillis();

            } else if(this.getResult() == CHAT_COOLDOWN) {
                player.sendMessage(TextFormat.colorize("&cYou must wait three seconds before sending another message."));
            } else if(this.getResult() == ALREADY_MUTED) {
                player.sendMessage("You're banned from chatting for " + Utils.calculateRemain(this.entry.getFinishAt()));
            } else {
                throw new IllegalStateException("Unexpected value: " + this.getResult());
            }
        }
    }
}