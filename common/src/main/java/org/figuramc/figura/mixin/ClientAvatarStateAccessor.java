package org.figuramc.figura.mixin;

import net.minecraft.client.entity.ClientAvatarState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientAvatarState.class)
public interface ClientAvatarStateAccessor {
    @Accessor("xCloak")
    double figura$xCloak();

    @Accessor("yCloak")
    double figura$yCloak();

    @Accessor("zCloak")
    double figura$zCloak();

    @Accessor("xCloakO")
    double figura$xCloakO();

    @Accessor("yCloakO")
    double figura$yCloakO();

    @Accessor("zCloakO")
    double figura$zCloakO();

    @Accessor("bob")
    float figura$bob();

    @Accessor("bobO")
    float figura$bobO();

    @Accessor("walkDist")
    float figura$walkDist();

    @Accessor("walkDistO")
    float figura$walkDistO();
}
