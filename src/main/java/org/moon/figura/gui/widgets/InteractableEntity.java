package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.gui.screens.AbstractPanelScreen;
import org.moon.figura.gui.screens.AvatarScreen;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

public class InteractableEntity extends AbstractContainerElement {

    public static final ResourceLocation UNKNOWN = new FiguraIdentifier("textures/gui/unknown_entity.png");
    public static final ResourceLocation OVERLAY = new FiguraIdentifier("textures/gui/entity_overlay.png");

    //properties
    private LivingEntity entity;
    private final float pitch, yaw, scale;
    private SwitchButton button;

    //transformation data

    //rot
    private boolean isRotating = false;
    private float anchorX = 0f, anchorY = 0f;
    private float anchorAngleX = 0f, anchorAngleY = 0f;
    private float angleX, angleY;

    //scale
    private float scaledValue = 0f;
    private static final float SCALE_FACTOR = 1.1F;

    //pos
    private boolean isDragging = false;
    private int modelX, modelY;
    private float dragDeltaX, dragDeltaY;
    private float dragAnchorX, dragAnchorY;

    public InteractableEntity(int x, int y, int width, int height, int scale, float pitch, float yaw, LivingEntity entity, AbstractPanelScreen parentScreen) {
        super(x, y, width, height);

        this.scale = scale;
        this.pitch = pitch;
        this.yaw = yaw;
        this.entity = entity;

        modelX = width / 2;
        modelY = height / 2;
        angleX = pitch;
        angleY = yaw;

        //button
        children.add(button = new SwitchButton(
                x + 4, y + 4, 16, 16,
                0, 0, 16,
                new FiguraIdentifier("textures/gui/expand.png"),
                48, 32,
                FiguraText.of("gui.expand"),
                bx -> {
                    if (button.isToggled()) {
                        //backup pos to fix model pos
                        int oldX = this.x;
                        int oldY = this.y;

                        //set screen (also updates pos/size)
                        Minecraft.getInstance().setScreen(new AvatarScreen(parentScreen, this));

                        //update button
                        button.x = this.x + 4;
                        button.y = this.y + 28;
                        button.setTooltip(FiguraText.of("gui.minimise"));

                        //update entity
                        this.modelX += oldX - this.x;
                        this.modelY += oldY - this.y;
                    } else {
                        Minecraft.getInstance().setScreen(parentScreen);
                        //no need to reset the widget since were not keeping this instance
                    }
                }));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible())
            return;

        if (!button.isToggled()) {
            //border
            UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
            //overlay
            UIHelper.renderTexture(stack, x + 1, y + 1, width - 2, height - 2, OVERLAY);
        }

        //scissors
        UIHelper.setupScissor(x + 1, y + 1, width - 2, height - 2);

        //render entity
        if (entity != null) {
            stack.pushPose();
            stack.translate(0f, 0f, -400f);
            UIHelper.drawEntity(x + modelX, y + modelY, scale + scaledValue, angleX, angleY, entity, stack, UIHelper.EntityRenderMode.FIGURA_GUI);
            stack.popPose();
        } else {
            stack.pushPose();

            //transforms
            stack.translate(x + modelX, y + modelY, 0f);
            float scale = this.scale / 35;
            stack.scale(scale, scale, scale);
            stack.mulPose(Quaternion.fromXYZDegrees(new Vector3f(angleX - pitch, angleY - yaw, 0f)));

            //draw front
            UIHelper.renderTexture(stack, -24, -32, 48, 64, UNKNOWN);

            //draw back
            stack.pushPose();
            stack.mulPose(Vector3f.YP.rotationDegrees(180));
            UIHelper.renderTexture(stack, -24, -32, 48, 64, UNKNOWN);
            stack.popPose();

            stack.popPose();
        }

        RenderSystem.disableScissor();

        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible() || !this.isMouseOver(mouseX, mouseY))
            return false;

        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        switch (button) {
            //left click - rotate
            case 0 -> {
                //set anchor rotation

                //get starter mouse pos
                anchorX = (float) mouseX;
                anchorY = (float) mouseY;

                //get starter rotation angles
                anchorAngleX = angleX;
                anchorAngleY = angleY;

                isRotating = true;
                return true;
            }

            //right click - move
            case 1 -> {
                //get starter mouse pos
                dragDeltaX = (float) mouseX;
                dragDeltaY = (float) mouseY;

                //also get start node pos
                dragAnchorX = modelX;
                dragAnchorY = modelY;

                isDragging = true;
                return true;
            }

            //middle click - reset pos
            case 2 -> {
                isRotating = false;
                isDragging = false;
                anchorX = 0f;
                anchorY = 0f;
                anchorAngleX = 0f;
                anchorAngleY = 0f;
                angleX = pitch;
                angleY = yaw;
                scaledValue = 0f;
                modelX = width / 2;
                modelY = height / 2;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //left click - stop rotating
        if (button == 0) {
            isRotating = false;
            return true;
        }

        //right click - stop dragging
        else if (button == 1) {
            isDragging = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //left click - rotate
        if (isRotating) {
            //get starter rotation angle then get hot much is moved and divided by a slow factor
            angleX = (float) (anchorAngleX + (anchorY - mouseY) / (3 / Minecraft.getInstance().getWindow().getGuiScale()));
            angleY = (float) (anchorAngleY - (anchorX - mouseX) / (3 / Minecraft.getInstance().getWindow().getGuiScale()));

            //cap to 360, so we don't get extremely high unnecessary rotation values
            if (angleX >= 360 || angleX <= -360) {
                anchorY = (float) mouseY;
                anchorAngleX = 0;
                angleX = 0;
            }
            if (angleY >= 360 || angleY <= -360) {
                anchorX = (float) mouseX;
                anchorAngleY = 0;
                angleY = 0;
            }

            return true;
        }

        //right click - move
        else if (isDragging) {
            //get how much it should move
            //get actual pos of the mouse, then subtract starter X,Y
            float x = (float) (mouseX - dragDeltaX);
            float y = (float) (mouseY - dragDeltaY);

            //move it
            modelX = (int) (dragAnchorX + x);
            modelY = (int) (dragAnchorY + y);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.isVisible())
            return false;

        if (super.mouseScrolled(mouseX, mouseY, amount))
            return true;

        //scroll - scale

        //set scale direction
        float scaleDir = (amount > 0) ? SCALE_FACTOR : 1 / SCALE_FACTOR;

        //determine scale
        scaledValue = ((scale + scaledValue) * scaleDir) - scale;

        return true;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }
}
