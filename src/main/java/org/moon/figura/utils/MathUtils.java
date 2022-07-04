package org.moon.figura.utils;

import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.ducks.GameRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.vector.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MathUtils {

    public static Object sizedVector(double... vals) {
        return switch (vals.length) {
            case 2 -> FiguraVec2.of(vals[0], vals[1]);
            case 3 -> FiguraVec3.of(vals[0], vals[1], vals[2]);
            case 4 -> FiguraVec4.of(vals[0], vals[1], vals[2], vals[3]);
            case 5 -> FiguraVec5.of(vals[0], vals[1], vals[2], vals[3], vals[4]);
            case 6 -> FiguraVec6.of(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
            default -> throw new IllegalStateException("Cannot create vector of size: " + vals.length);
        };
    }

    public static boolean getBool(long number, int byteAt) {
        int byteVal = (int) Math.pow(2, byteAt);
        return (number & byteVal) == byteVal;
    }

    public static double modulus(double number, double mod) {
        return ((number % mod) + mod) % mod;
    }

    //maya pls check those
    public static FiguraVec3 rotateAroundAxis(FiguraVec3 vec, FiguraVec3 axis, float degrees) {
        FiguraVec3 normalizedAxis = axis.normalized();
        Quaternion vectorQuat = new Quaternion((float) vec.x, (float) vec.y, (float) vec.z, 0);
        Quaternion rotatorQuat = new Quaternion(new Vector3f((float) normalizedAxis.x, (float) normalizedAxis.y, (float) normalizedAxis.z), degrees, true);
        Quaternion rotatorQuatConj = new Quaternion(rotatorQuat);
        rotatorQuatConj.conj();

        rotatorQuat.mul(vectorQuat);
        rotatorQuat.mul(rotatorQuatConj);

        normalizedAxis.free();
        return FiguraVec3.of(rotatorQuat.i(), rotatorQuat.j(), rotatorQuat.k());
    }

    public static FiguraVec3 toCameraSpace(FiguraVec3 vec) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        FiguraMat3 transformMatrix = FiguraMat3.fromMatrix3f(new Matrix3f(camera.rotation()));
        Vec3 pos = camera.getPosition();
        transformMatrix.invert();

        FiguraVec3 ret = vec.copy();
        ret.subtract(pos.x, pos.y, pos.z);
        ret.multiply(transformMatrix);
        ret.multiply(-1, 1, 1);

        transformMatrix.free();
        return ret;
    }

    public static FiguraVec4 worldToScreenSpace(FiguraVec3 worldSpace) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Matrix3f transformMatrix = new Matrix3f(camera.rotation());
        transformMatrix.invert();

        Vector3f camSpace = new Vector3f((float) worldSpace.x, (float) worldSpace.y, (float) worldSpace.z);
        Vec3 camPos = camera.getPosition();
        camSpace.sub(new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z));
        camSpace.transform(transformMatrix);

        Vector4f projectiveCamSpace = new Vector4f(camSpace);
        Matrix4f projMat = minecraft.gameRenderer.getProjectionMatrix(((GameRendererAccessor) minecraft.gameRenderer).figura$getFov(camera, minecraft.getFrameTime(), true));
        projectiveCamSpace.transform(projMat);
        float w = projectiveCamSpace.w();

        return FiguraVec4.of(projectiveCamSpace.x() / w, projectiveCamSpace.y() / w, projectiveCamSpace.z() / w, Math.sqrt(camSpace.dot(camSpace)));
    }

    private static final String[] SIZE_UNITS = {"b", "kb", "mb", "gb"};
    public static String asFileSize(double size) {
        int i = 0;
        while (i < SIZE_UNITS.length) {
            if (size < 1000) break;
            size /= 1000;
            i++;
        }

        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(size) + SIZE_UNITS[i];
    }
}
