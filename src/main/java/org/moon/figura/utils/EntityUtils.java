package org.moon.figura.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.moon.figura.mixin.ClientLevelInvoker;

import java.util.UUID;

public class EntityUtils {

    public static Entity getEntityByUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null)
            return null;
        return ((ClientLevelInvoker) Minecraft.getInstance().level).getEntityGetter().get(uuid);
    }

}
