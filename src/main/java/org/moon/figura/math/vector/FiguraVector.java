package org.moon.figura.math.vector;

import org.terasology.jnlua.LuaRuntimeException;

public abstract class FiguraVector<T extends FiguraVector<T>> {

    public abstract double lengthSquared();
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    public abstract T copy();
    public abstract double dot(T other);
    public abstract boolean equals(T other);

    public abstract void set(T other);
    public abstract void add(T other);
    public abstract void subtract(T other);
    public abstract void multiply(T other);
    public abstract void divide(T other);
    public abstract void reduce(T other);
    public abstract void iDivide(T other);
    public abstract void scale(double factor);
    public void normalize() {
        double len = length();
        if (len > 0)
            scale(1 / len);
    }
    public void clampLength(Double minLength, Double maxLength) {
        if (minLength == null) minLength = 0d;
        if (maxLength == null) maxLength = Double.POSITIVE_INFINITY;
        double len = length();
        if (len < minLength) {
            if (len == 0) throw new LuaRuntimeException("Attempt to divide by 0");
            scale(minLength / len);
        } else if (len > maxLength) {
            if (len == 0) throw new LuaRuntimeException("Attempt to divide by 0");
            scale(maxLength / len);
        }
    }

    public T plus(T other) {
        T result = copy();
        result.add(other);
        return result;
    }
    public T minus(T other) {
        T result = copy();
        result.subtract(other);
        return result;
    }
    public T times(T other) {
        T result = copy();
        result.multiply(other);
        return result;
    }
    public T dividedBy(T other) {
        T result = copy();
        result.divide(other);
        return result;
    }
    public T mod(T other) {
        T result = copy();
        result.reduce(other);
        return result;
    }
    public T iDividedBy(T other) {
        T result = copy();
        result.iDivide(other);
        return result;
    }
    public T scaled(double factor) {
        T result = copy();
        result.scale(factor);
        return result;
    }
    public T normalized() {
        T result = copy();
        result.normalize();
        return result;
    }
    public T toRad() {
        T result = copy();
        result.scale(Math.PI / 180);
        return result;
    }
    public T toDeg() {
        T result = copy();
        result.scale( 180 / Math.PI);
        return result;
    }
}
