package org.figuramc.figura.compat;

import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.compat.wrappers.ClassWrapper;
import org.figuramc.figura.compat.wrappers.FieldWrapper;
import org.figuramc.figura.compat.wrappers.MethodWrapper;

import java.util.Map;

public class GeckoLibCompat {

    // For newer versions of GeckoLib
    private static ClassWrapper GLRenderUtils;
    private static MethodWrapper getGeoModelForArmor;

    // For older versions of GeckoLib
    private static ClassWrapper GLGeoArmorRenderer;
    private static FieldWrapper renderers;
    private static FieldWrapper CONSTRUCTORS;

    public static void init() {
        GLRenderUtils = new ClassWrapper("software.bernie.geckolib.util.RenderUtils");
        getGeoModelForArmor = GLRenderUtils.getMethod("getGeoModelForArmor", ItemStack.class);

        GLGeoArmorRenderer = new ClassWrapper("software.bernie.geckolib3.renderers.geo.GeoArmorRenderer");
        // Fabric
        renderers = GLGeoArmorRenderer.getField("renderers");
        // Forge
        CONSTRUCTORS = GLGeoArmorRenderer.getField("CONSTRUCTORS");
    }

    public static boolean armorHasCustomModel(ItemStack stack) {
        if (GLRenderUtils.isLoaded) {
            if (getGeoModelForArmor.exists()) {
                return getGeoModelForArmor.invoke(null, stack) != null;
            }
            return false;
        }

        if (GLGeoArmorRenderer.isLoaded) {
            if (renderers.exists()) {
                if (renderers.getValue(null) instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) renderers.getValue(null);
                    return map.containsKey(stack.getItem().getClass());
                }
                renderers.markErrored();
            } else if (CONSTRUCTORS.exists()) {
                if (CONSTRUCTORS.getValue(null) instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) CONSTRUCTORS.getValue(null);
                    return map.containsKey(stack.getItem().getClass());
                }
            }

            return false;
        }


        return false;
    }
}
