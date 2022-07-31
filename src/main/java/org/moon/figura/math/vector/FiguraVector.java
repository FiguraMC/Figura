package org.moon.figura.math.vector;

import org.moon.figura.math.matrix.FiguraMatrix;
import org.moon.figura.utils.caching.CachedType;

public abstract class FiguraVector<T extends FiguraVector<T, M>, M extends FiguraMatrix<M, T>> implements CachedType<T> {

    public abstract double lengthSquared();
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public abstract T copy();
    public abstract double dot(T other);
    public abstract boolean equals(T other);
    public abstract T set(T other);


    public abstract T add(T other);
    public abstract T subtract(T other);
    public abstract T multiply(T other);
    public abstract T transform(M mat);
    public abstract T divide(T other);
    public abstract T reduce(T other);
    public abstract T scale(double factor);

    @SuppressWarnings("unchecked")
    public T normalize() {
        double len = length();
        if (len > 0)
            scale(1 / len);
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public T clampLength(Double minLength, Double maxLength) {
        if (minLength == null) minLength = 0d;
        if (maxLength == null) maxLength = Double.POSITIVE_INFINITY;
        double len = length();
        if (len == 0)
            return (T) this;
        if (len < minLength) {
            scale(minLength / len);
        } else if (len > maxLength) {
            scale(maxLength / len);
        }
        return (T) this;
    }

    public T clamped(Double minLength, Double maxLength) {
        return copy().clampLength(minLength, maxLength);
    }

    public T plus(T other) {
        return copy().add(other);
    }

    public T minus(T other) {
        return copy().subtract(other);
    }

    public T times(T other) {
        return copy().multiply(other);
    }

    public T dividedBy(T other) {
        return copy().divide(other);
    }

    public T mod(T other) {
        return copy().reduce(other);
    }

    public T scaled(double factor) {
        return copy().scale(factor);
    }

    public T normalized() {
        return copy().normalize();
    }

    public T toRad() {
        return copy().scale(Math.PI/180);
    }

    public T toDeg() {
        return copy().scale(180/Math.PI);
    }
}
