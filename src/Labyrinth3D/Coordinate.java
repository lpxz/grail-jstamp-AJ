package Labyrinth3D;

import java.lang.Math;
import common.Pair;

public class Coordinate {

    public int x;

    public int y;

    public int z;

    public Coordinate() {
    }

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Coordinate alloc(int x, int y, int z) {
        Coordinate c = new Coordinate(x, y, z);
        return c;
    }

    public static boolean isEqual(Coordinate a, Coordinate b) {
        if ((a.x == b.x) && (a.y == b.y) && (a.z == b.z)) return true;
        return false;
    }

    private static double getPairDistance(Pair p) {
        Coordinate a = (Coordinate) p.first;
        Coordinate b = (Coordinate) p.second;
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        int dz = a.z - b.z;
        int dx2 = dx * dx;
        int dy2 = dy * dy;
        int dz2 = dz * dz;
        return Math.sqrt((double) (dx2 + dy2 + dz2));
    }

    public static int comparePair(final Object a, final Object b) {
        double aDistance = getPairDistance((Pair) a);
        double bDistance = getPairDistance((Pair) b);
        if (aDistance < bDistance) {
            return 1;
        } else if (aDistance > bDistance) {
            return -1;
        }
        return 0;
    }

    public static boolean areAdjacent(Coordinate a, Coordinate b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        int dz = a.z - b.z;
        int dx2 = dx * dx;
        int dy2 = dy * dy;
        int dz2 = dz * dz;
        return (((dx2 + dy2 + dz2) == 1) ? true : false);
    }
}
