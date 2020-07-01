package us.advancedserver.listener;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import org.apache.commons.math3.util.FastMath;
import us.advancedserver.AdvancedPlayer;
import us.advancedserver.AdvancedServer;
import us.advancedserver.utils.ElementButton;
import us.advancedserver.utils.EntityPearl;
import us.advancedserver.utils.SnowGolem;
import us.advancedserver.utils.Utils;

import java.util.Objects;

public class EventListener implements Listener {

    public EventListener() {
        Server.getInstance().getPluginManager().registerEvents(this, AdvancedServer.getInstance());

        Entity.registerEntity("CustomSnowGolem", SnowGolem.class);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        AdvancedPlayer player = (AdvancedPlayer) ev.getPlayer();

        String name = TextFormat.clean(ev.getItem().getCustomName());

        if(name.equalsIgnoreCase("Loot (Right click)")) {
            FormWindowSimple form = new FormWindowSimple(TextFormat.colorize("&4&lLoot cosmetic"), TextFormat.colorize("&fSelect a type cosmetic"));

            for(String buttonText : new String[]{"Snow Golem", "Sheep Explode", "Light strike", "Size", "Particles", "Launch"}) {
                form.addButton(new ElementButton(TextFormat.colorize("&5") + buttonText, buttonText));
            }

            player.showFormWindow(form, 93484);
        } else if(name.equalsIgnoreCase("Sheep Explode (Right click)")) {
            double f = 1.3D;

            double yaw = player.yaw + cn.nukkit.utils.Utils.rand(- 12.0D, 12.0D);

            double pitch = player.pitch + cn.nukkit.utils.Utils.rand(- 7.0D, 7.0D);

            Location pos = new Location(player.x - Math.sin(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * 0.5D, player.y + (double) player.getHeight() - 0.18D, player.z + Math.cos(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * 0.5D, yaw, pitch, player.level);

            if(player.getLevel().getBlockIdAt((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()) == 0) {
                EntityPearl entity = (EntityPearl) Entity.createEntity("EntityPearl", pos, player);

                if(entity == null) return;

                entity.setMotion(new Vector3(- Math.sin(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * f * f, - Math.sin(FastMath.toRadians(pitch)) * f * f, Math.cos(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * f * f));

                entity.spawnToAll();
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent ev) {
        if(ev.getEntity() instanceof EntityPearl) {
            EntityPearl entity = (EntityPearl) ev.getEntity();

            AdvancedPlayer player = (AdvancedPlayer) entity.shootingEntity;

            assert player != null;

            SnowGolem e = (SnowGolem) Entity.createEntity("CustomSnowGolem", entity.getPosition());

            if(e == null) return;

            e.setOwnerName(player.getName());

            e.spawnToAll();
        }
    }

    @EventHandler
    public void onFormResponded(PlayerFormRespondedEvent ev) {
        if(ev.wasClosed()) return;

        AdvancedPlayer player = (AdvancedPlayer) ev.getPlayer();

        Objects.requireNonNull(player, "Variable player was received null");

        if(ev.getFormID() == 93484) {
            Utils.giveCurrentlyLoot(player, ((ElementButton) ((FormResponseSimple) ev.getResponse()).getClickedButton()).getDefinition());
        } else if(ev.getFormID() == 39483) {
            player.setCurrentlyLoot("Particles", ((ElementButton) ((FormResponseSimple) ev.getResponse()).getClickedButton()).getDefinition());

            player.sendMessage(TextFormat.BLUE + "You successfully activated the particle: " + player.getCurrentlyParticle());
        } else if(ev.getFormID() == 47845) {
            FormResponseCustom response = (FormResponseCustom) ev.getResponse();

            player.setCurrentlyLoot("Size", (float) Integer.parseInt(response.getStepSliderResponse(0).getElementContent()));

            player.sendMessage(TextFormat.BLUE + "Your skin size is now: " + player.getCurrentlySize());
        }
    }
}