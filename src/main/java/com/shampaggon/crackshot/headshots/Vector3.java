package com.shampaggon.crackshot.headshots;

/**
 * Минимальный 3D-вектор для собственных расчётов headshot-логики.
 */
public class Vector3 {
    public double x;
    public double y;
    public double z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Даёт доступ к координате по индексу оси: 0=x, 1=y, 2=z.
    public double get(int axis) {
        return axis == 0 ? this.x : axis == 1 ? this.y : this.z;
    }
}
