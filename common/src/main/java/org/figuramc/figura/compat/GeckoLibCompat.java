package org.figuramc.figura.compat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.compat.wrappers.ClassWrapper;
import org.figuramc.figura.compat.wrappers.FieldWrapper;
import org.figuramc.figura.compat.wrappers.MethodWrapper;
import java.util.Map;

public class GeckoLibCompat {

    // For newer versions of GeckoLib
    private static ClassWrapper GLRenderUtils;
    private static MethodWrapper getGeoModelForArmor;
    private static ClassWrapper renderProvider;
    private static MethodWrapper renderProviderOf;
    private static MethodWrapper getGenericArmorModel;

    // For older versions of GeckoLib
    private static ClassWrapper GLGeoArmorRenderer;
    private static FieldWrapper renderers;


    public static void init() {
        GLRenderUtils = new ClassWrapper("software.bernie.geckolib.util.RenderUtils");
        getGeoModelForArmor = GLRenderUtils.getMethod("getGeoModelForArmor", ItemStack.class);
        renderProvider =  new ClassWrapper("software.bernie.geckolib.animatable.client.RenderProvider");
        renderProviderOf = renderProvider.getMethod("of", ItemStack.class);
        getGenericArmorModel = renderProvider.getMethod("getGenericArmorModel", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class);


        GLGeoArmorRenderer = new ClassWrapper("software.bernie.geckolib3.renderers.geo.GeoArmorRenderer");
        renderers = GLGeoArmorRenderer.getField("renderers");
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
                if (renderers.getValue(null) instanceof Map<?, ?> map) {
                    return map.containsKey(stack.getItem().getClass());
                }
                renderers.markErrored();
            }

            return false;
        }


        return false;
    }

    public static <T extends LivingEntity, A extends HumanoidModel<T>> A getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, A humanoidModel) {
        if (renderProvider.isLoaded) {
            if (renderProviderOf.exists() && getGenericArmorModel.exists()){
                Object renderProvider = renderProviderOf.invoke(null, stack);
                if (renderProvider != null) {
                    return (A) getGenericArmorModel.invoke(renderProvider, entity, stack, slot, humanoidModel);
                }
            }
        }
        return humanoidModel;
    }
}
