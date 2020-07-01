package us.advancedserver.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.apache.commons.math3.util.FastMath;
import us.advancedserver.AdvancedPlayer;

public class SnowGolem extends EntityLiving {

    private String ownerName;

    private int attackDelay = 0;

    private float createdAt = 0;

    public SnowGolem(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;

        this.createdAt = System.currentTimeMillis();
    }

    public int getNetworkId() {
        return 21;
    }

    public float getWidth() {
        return 0.7F;
    }

    public float getHeight() {
        return 1.9F;
    }

    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(4);
    }

    public AdvancedPlayer getTarget() {
        AdvancedPlayer target = null;

        double near = 2.147483647E9D;

        Entity[] e = this.getLevel().getEntities();

        for(Entity entity : e) {
            if(entity instanceof AdvancedPlayer && (! entity.getName().equalsIgnoreCase(this.ownerName))) {
                double distance = this.distanceSquared(entity);

                if(distance <= near) {
                    near = distance;

                    target = (AdvancedPlayer) entity;
                }
            }
        }

        return target;
    }

    public void attackEntity(Entity player) {
        if(player instanceof AdvancedPlayer && this.attackDelay > 23 && cn.nukkit.utils.Utils.rand(1, 32) < 10 && this.distanceSquared(player) <= 55.0D) {

            this.attackDelay = 0;

            double f = 1.2D;

            double yaw = this.yaw + cn.nukkit.utils.Utils.rand(- 12.0D, 12.0D);

            double pitch = this.pitch + cn.nukkit.utils.Utils.rand(- 7.0D, 7.0D);

            Location location = new Location(this.x + - Math.sin(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * 0.5D, this.y + (double) this.getEyeHeight(), this.z + Math.cos(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * 0.5D, yaw, pitch, this.level);

            EntitySnowball k = (EntitySnowball) Entity.createEntity("Snowball", location, this);

            if(k == null) {
                return;
            }

            k.setMotion((new Vector3(- Math.sin(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * f * f, - Math.sin(FastMath.toRadians(pitch)) * f * f, Math.cos(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * f * f)).multiply(f));

            EntityShootBowEvent ev = new EntityShootBowEvent(this, Item.get(262, 0, 1), k, f);

            this.server.getPluginManager().callEvent(ev);

            EntityProjectile projectile = ev.getProjectile();

            if(ev.isCancelled()) {
                projectile.close();
            } else if(projectile != null) {
                ProjectileLaunchEvent launch = new ProjectileLaunchEvent(projectile);

                this.server.getPluginManager().callEvent(launch);

                if(launch.isCancelled()) {
                    projectile.close();
                } else {
                    projectile.spawnToAll();
                }
            }
        }
    }

    public String getName() {
        return "Snow Golem";
    }

    public boolean entityBaseTick(int tickDiff) {
        if(this.attackDelay < 200) ++this.attackDelay;

        return super.entityBaseTick(tickDiff);
    }

    public boolean onUpdate(int currentTick) {
        this.entityBaseTick(currentTick - this.lastUpdate);

        if(this.createdAt != 0 && ((System.currentTimeMillis() - this.createdAt) / 1000) > 10) {
            this.close();
        } else {
            this.attackEntity(this.getTarget());
        }

        return super.onUpdate(currentTick);
    }
}
