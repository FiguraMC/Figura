package org.moon.figura.utils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.moon.figura.ducks.GameRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.matrix.FiguraMatrix;
import org.moon.figura.math.vector.*;

import java.lang.Math;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MathUtils {

    public static FiguraVector<?, ?> sizedVector(double... vals) {
        return switch (vals.length) {
            case 2 -> FiguraVec2.of(vals[0], vals[1]);
            case 3 -> FiguraVec3.of(vals[0], vals[1], vals[2]);
            case 4 -> FiguraVec4.of(vals[0], vals[1], vals[2], vals[3]);
            case 5 -> FiguraVec5.of(vals[0], vals[1], vals[2], vals[3], vals[4]);
            case 6 -> FiguraVec6.of(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
            default -> throw new IllegalStateException("Cannot create vector of size: " + vals.length);
        };
    }

    public static FiguraMatrix<?, ?> sizedMat(FiguraVector<?, ?>... vectors) {
        return switch (vectors.length) {
            case 4 -> FiguraMat4.of(
                    vectors[0].index(0), vectors[0].index(1), vectors[0].index(2), vectors[0].index(3),
                    vectors[1].index(0), vectors[1].index(1), vectors[1].index(2), vectors[1].index(3),
                    vectors[2].index(0), vectors[2].index(1), vectors[2].index(2), vectors[2].index(3),
                    vectors[3].index(0), vectors[3].index(1), vectors[3].index(2), vectors[3].index(3)
            );
            case 3 -> FiguraMat3.of(
                    vectors[0].index(0), vectors[0].index(1), vectors[0].index(2),
                    vectors[1].index(0), vectors[1].index(1), vectors[1].index(2),
                    vectors[2].index(0), vectors[2].index(1), vectors[2].index(2)
            );
            case 2 -> FiguraMat2.of(
                    vectors[0].index(0), vectors[0].index(1),
                    vectors[1].index(0), vectors[1].index(1)
            );
            default -> throw new IllegalStateException("Cannot create matrix of size: " + vectors.length);
        };
    }

    //maya pls check those //ty <3 <3
    public static FiguraVec3 rotateAroundAxis(FiguraVec3 vec, FiguraVec3 axis, double degrees) {
        FiguraVec3 normalizedAxis = axis.normalized();
        Quaternionf vectorQuat = new Quaternionf((float) vec.x, (float) vec.y, (float) vec.z, 0);
        Quaternionf rotatorQuat = new Quaternionf().fromAxisAngleDeg((float) normalizedAxis.x, (float) normalizedAxis.y, (float) normalizedAxis.z, (float) degrees);
        Quaternionf rotatorQuatConj = new Quaternionf(rotatorQuat);
        rotatorQuatConj.conjugate();

        rotatorQuat.mul(vectorQuat);
        rotatorQuat.mul(rotatorQuatConj);

        normalizedAxis.free();
        return FiguraVec3.of(rotatorQuat.x(), rotatorQuat.y(), rotatorQuat.z());
    }

    public static FiguraVec3 toCameraSpace(FiguraVec3 vec) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        FiguraMat3 transformMatrix = FiguraMat3.fromMatrix3f(new Matrix3f().rotation(camera.rotation()));
        Vec3 pos = camera.getPosition();
        transformMatrix.invert();

        FiguraVec3 ret = vec.copy();
        ret.subtract(pos.x, pos.y, pos.z);
        ret.transform(transformMatrix);
        //ret.multiply(-1, 1, 1);

        transformMatrix.free();
        return ret;
    }

    public static FiguraVec4 worldToScreenSpace(FiguraVec3 worldSpace) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Matrix3f transformMatrix = new Matrix3f().rotation(camera.rotation());
        transformMatrix.invert();

        Vector3f camSpace = new Vector3f((float) worldSpace.x, (float) worldSpace.y, (float) worldSpace.z);
        Vec3 camPos = camera.getPosition();
        camSpace.sub(new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z));
        transformMatrix.transform(camSpace);

        Vector4f projectiveCamSpace = new Vector4f(camSpace, 1f);
        Matrix4f projMat = minecraft.gameRenderer.getProjectionMatrix(((GameRendererAccessor) minecraft.gameRenderer).figura$getFov(camera, minecraft.getFrameTime(), true));
        projMat.transform(projectiveCamSpace);
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

    public static FiguraVec3 clamp(FiguraVec3 val, double min, double max) {
        return FiguraVec3.of(
                Math.min(Math.max(val.x, min), max),
                Math.min(Math.max(val.y, min), max),
                Math.min(Math.max(val.z, min), max)
        );
    }

    public static FiguraVec3 lerp(double delta, FiguraVec3 prev, FiguraVec3 next) {
        return FiguraVec3.of(
                lerp(delta, prev.x, next.x),
                lerp(delta, prev.y, next.y),
                lerp(delta, prev.z, next.z)
        );
    }

    public static double lerp(double delta, double prev, double next) {
        return delta * (next - prev) + prev;
    }

    public static FiguraVec3 catmullrom(double delta, FiguraVec3 prevA, FiguraVec3 prevB, FiguraVec3 nextA, FiguraVec3 nextB) {
        return FiguraVec3.of(
                catmullrom(delta, prevA.x, prevB.x, nextA.x, nextB.x),
                catmullrom(delta, prevA.y, prevB.y, nextA.y, nextB.y),
                catmullrom(delta, prevA.z, prevB.z, nextA.z, nextB.z)
        );
    }

    //no idea how it works
    //it is the same function from minecraft, but using doubles instead
    public static double catmullrom(double delta, double prevA, double prevB, double nextA, double nextB) {
        return 0.5 * (2 * prevB + (nextA - prevA) * delta + (2 * prevA - 5 * prevB + 4 * nextA - nextB) * delta * delta + (3 * prevB - prevA - 3 * nextA + nextB) * delta * delta * delta);
    }

    public static FiguraVec3 bezier(double delta, FiguraVec3 p0, FiguraVec3 p1, FiguraVec3 p2, FiguraVec3 p3) {
        return FiguraVec3.of(
                bezier(delta, p0.x, p1.x, p2.x, p3.x),
                bezier(delta, p0.y, p1.y, p2.y, p3.y),
                bezier(delta, p0.z, p1.z, p2.z, p3.z)
        );
    }

    //bezier function generated by ChatGPT
    public static double bezier(double delta, double p0, double p1, double p2, double p3) {
        return (1 - delta) * (1 - delta) * (1 - delta) * p0 + 3 * (1 - delta) * (1 - delta) * delta * p1 + 3 * (1 - delta) * delta * delta * p2 + delta * delta * delta * p3;
    }
}
