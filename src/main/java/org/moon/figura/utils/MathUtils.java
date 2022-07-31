package org.moon.figura.utils;

import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.ducks.GameRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.math.vector.FiguraVec5;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MathUtils {

    @Deprecated
    public static Object oldSizedVector(double... vals) {
        return switch (vals.length) {
            case 2 -> org.moon.figura.math.vector.FiguraVec2.of(vals[0], vals[1]);
            case 3 -> org.moon.figura.math.vector.FiguraVec3.of(vals[0], vals[1], vals[2]);
            case 4 -> org.moon.figura.math.vector.FiguraVec4.of(vals[0], vals[1], vals[2], vals[3]);
            case 5 -> org.moon.figura.math.vector.FiguraVec5.of(vals[0], vals[1], vals[2], vals[3], vals[4]);
            case 6 -> org.moon.figura.math.vector.FiguraVec6.of(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
            default -> throw new IllegalStateException("Cannot create vector of size: " + vals.length);
        };
    }

    public static Object sizedVector(double... vals) {
        return switch (vals.length) {
            case 2 -> FiguraVec2.of(vals[0], vals[1]);
            case 3 -> FiguraVec3.of(vals[0], vals[1], vals[2]);
            case 4 -> FiguraVec4.of(vals[0], vals[1], vals[2], vals[3]);
            case 5 -> FiguraVec5.of(vals[0], vals[1], vals[2], vals[3], vals[4]);
            case 6 -> org.moon.figura.math.vector.FiguraVec6.of(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
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

    //maya pls check those //ty <3 <3
    public static FiguraVec3 rotateAroundAxis(FiguraVec3 vec, FiguraVec3 axis, double degrees) {
        FiguraVec3 normalizedAxis = axis.normalized();
        Quaternion vectorQuat = new Quaternion((float) vec.x, (float) vec.y, (float) vec.z, 0);
        Quaternion rotatorQuat = new Quaternion(new Vector3f((float) normalizedAxis.x, (float) normalizedAxis.y, (float) normalizedAxis.z), (float) degrees, true);
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
        ret.transform(transformMatrix);
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

    public static double lerp(double delta, double prev, double next) {
        return delta * (next - prev) + prev;
    }

    //no idea how it works
    //it is the same function from minecraft, but using doubles instead
    public static double catmullrom(double delta, double prevA, double prevB, double nextA, double nextB) {
        return 0.5 * (2 * prevB + (nextA - prevA) * delta + (2 * prevA - 5 * prevB + 4 * nextA - nextB) * delta * delta + (3 * prevB - prevA - 3 * nextA + nextB) * delta * delta * delta);
    }

    //same as minecraft too, but with doubles and fixing the NaN in the Math.asin
    public static FiguraVec3 quaternionToYXZ(Quaternion quaternion) {
        double r, i, j, k;
        r = quaternion.r();
        i = quaternion.i();
        j = quaternion.j();
        k = quaternion.k();

        double f = r * r;
        double g = i * i;
        double h = j * j;
        double m = k * k;
        double n = f + g + h + m;
        double o = 2 * r * i - 2 * j * k;
        double l = Math.asin(Math.max(Math.min(1, o / n), -1));
        return Math.abs(o) > 0.999d * n ?
                FiguraVec3.of(l, 2 * Math.atan2(j, r), 0) :
                FiguraVec3.of(l, Math.atan2(2 * i * k + 2 * j * r, f - g - h + m), Math.atan2(2 * i * j + 2 * r * k, f - g + h - m));
    }
}
