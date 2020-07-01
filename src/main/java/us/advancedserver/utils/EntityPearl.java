package us.advancedserver.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityEnderPearl;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPearl extends EntityEnderPearl {

    public EntityPearl(FullChunk chunk, CompoundTag nbt, Entity shooting) {
        super(chunk, nbt, shooting);
    }
}
