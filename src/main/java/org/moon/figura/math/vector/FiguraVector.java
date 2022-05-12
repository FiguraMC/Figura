package org.moon.figura.math.vector;

public interface FiguraVector<T extends FiguraVector<T>> {
    double lengthSquared();
    double length();
    T copy();
    double dot(T other);
    boolean equals(T other);

    void set(T other);
    void add(T other);
    void subtract(T other);
    void multiply(T other);
    void divide(T other);
    void reduce(T other);
    void iDivide(T other);
    void scale(double factor);
    void normalize();

    T plus(T other);
    T minus(T other);
    T times(T other);
    T dividedBy(T other);
    T mod(T other);
    T iDividedBy(T other);
    T scaled(double factor);
    T normalized();
}
