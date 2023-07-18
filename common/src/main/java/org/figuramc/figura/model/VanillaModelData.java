<<<<<<< HEAD:common/src/main/java/org/figuramc/figura/model/VanillaModelData.java
package org.moon.figura.model;
=======
package org.figuramc.figura.model;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:src/main/java/org/moon/figura/avatars/model/VanillaModelData.java

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
<<<<<<< HEAD:common/src/main/java/org/figuramc/figura/model/VanillaModelData.java
import org.moon.figura.math.vector.FiguraVec3;
=======
import org.figuramc.figura.math.vector.FiguraVec3;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:src/main/java/org/moon/figura/avatars/model/VanillaModelData.java

import java.util.HashMap;
import java.util.Map;

public class VanillaModelData {

    public final Map<ParentType, PartData> partMap = new HashMap<>() {{
        for (ParentType value : ParentType.values()) {
            if (value.provider != null)
                put(value, new PartData());
        }
    }};

    public void update(LivingEntityRenderer<?, ?> entityRenderer) {
        for (Map.Entry<ParentType, PartData> entry : partMap.entrySet()) {
            ParentType parent = entry.getKey();

            EntityModel<?> vanillaModel;
            vanillaModel = entityRenderer.getModel();

            if (vanillaModel == null)
                continue;

            update(parent, vanillaModel);
        }
    }

    public void update(ParentType parent, EntityModel<?> model) {
        ModelPart part = parent.provider.func.apply(model);
        if (part == null)
            return;

        update(parent, part);
    }

    public void update(ParentType parent, ModelPart part) {
        PartData data = partMap.get(parent);
        if (data != null)
            data.updateFromPart(part);
    }

    public static class PartData {

        public final FiguraVec3 pos = FiguraVec3.of();
        public final FiguraVec3 rot = FiguraVec3.of();
        public final FiguraVec3 scale = FiguraVec3.of(1, 1, 1);
        public boolean visible = false;

        private void updateFromPart(ModelPart model) {
            this.pos.set(model.x, model.y, -model.z);
            this.rot.set(Math.toDegrees(-model.xRot), Math.toDegrees(-model.yRot), Math.toDegrees(model.zRot));
            //this.scale.set(model.xScale, model.yScale, model.zScale);
            this.visible = model.visible;
        }
    }
}
