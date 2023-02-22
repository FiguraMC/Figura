package org.moon.figura.utils;

import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.ducks.GameRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.matrix.FiguraMatrix;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.math.vector.FiguraVector;

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
        Quaternion vectorQuat = new Quaternion((float) vec.x, (float) vec.y, (float) vec.z, 0);
        Quaternion rotatorQuat = new Quaternion(new Vector3f((float) normalizedAxis.x, (float) normalizedAxis.y, (float) normalizedAxis.z), (float) degrees, true);
        Quaternion rotatorQuatConj = new Quaternion(rotatorQuat);
        rotatorQuatConj.conj();

        rotatorQuat.mul(vectorQuat);
        rotatorQuat.mul(rotatorQuatConj);

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

    public static FiguraVec3 min(FiguraVec3 val, double min) {
        return FiguraVec3.of(
                Math.min(val.x, min),
                Math.min(val.y, min),
                Math.min(val.z, min)
        );
    }

    public static FiguraVec3 max(FiguraVec3 val, double max) {
        return FiguraVec3.of(
                Math.max(val.x, max),
                Math.max(val.y, max),
                Math.max(val.z, max)
        );
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

    //bezier function generated by ChatGPT
    public static double bezier(double t, double p0, double p1, double p2, double p3) {
        return p0 * (1 - t) * (1 - t) * (1 - t) + 3 * p1 * (1 - t) * (1 - t) * t + 3 * p2 * (1 - t) * t * t + p3 * t * t * t;
    }

    //binary search bezier find T method, also provided by ChatGPT
    public static double bezierFindT(double x, double p0, double p1, double p2, double p3) {
        double lower = 0.0;
        double upper = 1.0;
        double t = 0.5;
        double errorThreshold = 0.001;
        double xTolerance = 0.001;
        int maxIterations = 100;

        for (int i = 0; i < maxIterations; i++) {
            double cx = 3 * (p1 - p0);
            double bx = 3 * (p2 - p1) - cx;
            double ax = p3 - p0 - cx - bx;

            double xT = ax * t * t * t + bx * t * t + cx * t + p0;
            double diff = xT - x;

            if (Math.abs(diff) < xTolerance) {
                return t;
            } else if (diff > 0) {
                upper = t;
                t = (lower + upper) / 2.0;
            } else {
                lower = t;
                t = (lower + upper) / 2.0;
            }

            if (Math.abs(upper - lower) < errorThreshold) {
                return t;
            }
        }

        return t; //failed to converge, however return wherever we were
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
