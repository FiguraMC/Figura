package org.moon.figura.lua.api.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;

import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelPart",
        description = "vanilla_part"
)
public class VanillaModelPart implements VanillaPart {

    private final ParentType parentType;
    private final Function<EntityModel<?>, ModelPart> provider;

    private final FiguraVec3 savedOriginRot = FiguraVec3.of();
    private final FiguraVec3 savedOriginPos = FiguraVec3.of();

    private boolean visible = true;
    private boolean storedVisibility;

    public VanillaModelPart(ParentType parentType, Function<EntityModel<?>, ModelPart> provider) {
        this.parentType = parentType;
        this.provider = provider;
    }

    @Override
    public void alter(EntityModel<?> model) {
        if (provider == null)
            return;

        ModelPart part = provider.apply(model);
        storedVisibility = part.visible;
        part.visible = visible;
    }

    @Override
    public void store(EntityModel<?> model) {
        if (provider == null)
            return;

        ModelPart part = provider.apply(model);
        savedOriginRot.set(-part.xRot, -part.yRot, part.zRot);
        savedOriginRot.scale(180/Math.PI);

        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        savedOriginPos.set(pivot);
        pivot.free();
    }

    @Override
    public void restore(EntityModel<?> model) {
        if (provider != null)
            provider.apply(model).visible = storedVisibility;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {VanillaModelPart.class, Boolean.class},
                    argumentNames = {"vanillaPart", "visible"}
            ),
            description = "vanilla_part.set_visible"
    )
    public static void setVisible(@LuaNotNil VanillaModelPart vanillaPart, @LuaNotNil Boolean visible) {
        vanillaPart.setVisible(visible);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = VanillaModelPart.class,
                    argumentNames = "vanillaPart"
            ),
            description = "vanilla_part.get_visible"
    )
    public static Boolean getVisible(@LuaNotNil VanillaModelPart vanillaPart) {
        return vanillaPart.isVisible();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = VanillaModelPart.class,
                    argumentNames = "vanillaPart"
            ),
            description = "vanilla_part.get_origin_rot"
    )
    public static FiguraVec3 getOriginRot(@LuaNotNil VanillaModelPart vanillaPart) {
        return vanillaPart.savedOriginRot.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = VanillaModelPart.class,
                    argumentNames = "vanillaPart"
            ),
            description = "vanilla_part.get_origin_pos"
    )
    public static FiguraVec3 getOriginPos(@LuaNotNil VanillaModelPart vanillaPart) {
        return vanillaPart.savedOriginPos.copy();
    }

    @Override
    public String toString() {
        return "VanillaModelPart";
    }
}
