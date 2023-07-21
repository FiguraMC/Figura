package org.figuramc.figura.math.matrix;

import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.math.vector.FiguraVector;

public abstract class FiguraMatrix<T extends FiguraMatrix<T, V>, V extends FiguraVector<V, T>> {

    protected T cachedInverse = null;
    protected double cachedDeterminant = Double.NaN;
    protected FiguraMatrix() {}

    protected abstract double calculateDeterminant();
    protected abstract void resetIdentity();
    public abstract T copy();
    public abstract boolean equals(T other);
    public abstract V getColumn(int col);
    public abstract V getRow(int row);
    public abstract int rows();
    public abstract int cols();

    public abstract T set(T o);
    public abstract T multiply(T o);
    // "other" is on the right side.
    public abstract T rightMultiply(T o);
    public abstract T transpose();
    public abstract T invert();
    public abstract T add(T o);
    public abstract T sub(T o);

    public T transposed() {
        return copy().transpose();
    }
    public T inverted() {
        return copy().invert();
    }
    // Returns the product of the matrices, with "o" on the left.
    public T times(T o) {
        return copy().multiply(o);
    }
    public V times(V vec) {
        return vec.copy().transform((T) this);
    }

    public T plus(T o) {
        return copy().add(o);
    }
    public T minus(T o) {
        return copy().sub(o);
    }

    public T reset() {
        cachedDeterminant = Double.NaN;
        cachedInverse = null;
        resetIdentity();
        return (T) this;
    }

    public double det() {
        if (!Double.isNaN(cachedDeterminant))
            return cachedDeterminant;
        cachedDeterminant = calculateDeterminant();
        if (cachedDeterminant == 0)
            cachedDeterminant = Double.MIN_VALUE;
        return cachedDeterminant;
    }

    protected final void invalidate() {
        cachedInverse = null;
        cachedDeterminant = Double.NaN;
    }

    protected String getString(Double... d) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[");

        int mod = cols();

        for (int i = 0; i < d.length; i++) {
            if (i % mod == 0)
                sb.append("\n\t");
            sb.append(FiguraLuaPrinter.df.format(d[i]));
            if (i < d.length - 1)
                sb.append(", ");
        }

        return sb.append("\n]").toString();
    }

    public static abstract class DummyMatrix<V extends FiguraVector<V, DummyMatrix<V>>> extends FiguraMatrix<DummyMatrix<V>, V> {}

}
