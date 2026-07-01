package com.shampaggon.crackshot.headshots;

/**
 * Простая axis-aligned bounding box для расчёта попаданий в область головы.
 */
public class Aabb {
    private static final double EPSILON = 1.0E-8d;
    public Vector3 center;
    public Vector3 halfExtent;

    public Aabb(Vector3 center, Vector3 halfExtent) {
        this.center = center;
        this.halfExtent = halfExtent;
    }

    // Проверяет пересечение луча с боксом методом slab intersection.
    public static boolean intersectsLine(Vector3 point, Vector3 direction, Aabb box) {
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 3; i++) {
            double origin = point.get(i);
            double dir = direction.get(i);
            double boxMin = box.center.get(i) - box.halfExtent.get(i);
            double boxMax = box.center.get(i) + box.halfExtent.get(i);
            if (Math.abs(dir) >= EPSILON) {
                double t1 = (boxMin - origin) / dir;
                double t2 = (boxMax - origin) / dir;
                if (t1 > t2) {
                    t1 = t2;
                    t2 = t1;
                }
                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);
                if (tMin > tMax) {
                    return false;
                }
            } else if (origin < boxMin || origin > boxMax) {
                return false;
            }
        }
        return true;
    }
}
