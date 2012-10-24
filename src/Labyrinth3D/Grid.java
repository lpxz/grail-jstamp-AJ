package Labyrinth3D;

import java.lang.Long;
import java.util.Arrays;

public class Grid {
    
	/*atomicset(G)*/
    private static int GRID_POINT_FULL = -2;

    private static int GRID_POINT_EMPTY = -1;

 /*atomic(G)*/    public int width;

/*atomic(G)*/    public int height;

/*atomic(G)*/    public int depth;

    public int[][][] points_unaligned;

    public Grid() {
    }

    public static Grid alloc(int width, int height, int depth) {
        Grid grid = new Grid();
        grid.width = width;
        grid.height = height;
        grid.depth = depth;
        int[][][] points_unaligned = new int[width][height][depth];
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) for (int k = 0; k < depth; k++) points_unaligned[i][j][k] = GRID_POINT_EMPTY;
        grid.points_unaligned = points_unaligned;
        return grid;
    }

    public static Grid scratchalloc(int width, int height, int depth) {
        Grid grid = new Grid();
        grid.width = width;
        grid.height = height;
        grid.depth = depth;
        int[][][] points_unaligned = new int[width][height][depth];
        grid.points_unaligned = points_unaligned;
        return grid;
    }

    public static void copy(Grid dstGridPtr, Grid srcGridPtr) {
        if ((srcGridPtr.width == dstGridPtr.width) || (srcGridPtr.height == dstGridPtr.height) || (srcGridPtr.depth == dstGridPtr.depth)) {
            int width = Math.min(srcGridPtr.width, dstGridPtr.width);
            int height = Math.min(srcGridPtr.height, dstGridPtr.height);
            int depth = Math.min(srcGridPtr.depth, dstGridPtr.depth);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    for (int k = 0; k < depth; k++) {
                        dstGridPtr.points_unaligned[i][j][k] = srcGridPtr.points_unaligned[i][j][k];
                    }
                }
            }
        }
    }

    private static void copy2(int[][][] src, int[][][] dst) {
    }

    public boolean isPointValid(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public int getPoint(int x, int y, int z) {
        return this.points_unaligned[x][y][z];
    }

    public boolean isPointEmpty(int x, int y, int z) {
        return points_unaligned[x][y][z] == GRID_POINT_EMPTY;
    }

    public boolean isPointFull(int x, int y, int z) {
        return points_unaligned[x][y][z] == GRID_POINT_FULL;
    }

    public void setPoint(int x, int y, int z, int value) {
        points_unaligned[x][y][z] = value;
    }

    public void addPath(Vector_t pointVectorPtr) {
        int i;
        int n = pointVectorPtr.vector_getSize();
        for (i = 0; i < n; i++) {
            Coordinate coordinatePtr = (Coordinate) pointVectorPtr.vector_at(i);
            int x = coordinatePtr.x;
            int y = coordinatePtr.y;
            int z = coordinatePtr.z;
            points_unaligned[x][y][z] = GRID_POINT_FULL;
        }
    }

    public boolean TM_addPath(Vector_t pointVectorPtr) {
        int i;
        int n = pointVectorPtr.vector_getSize();
        int height = this.height;
        int width = this.width;
        int area = height * width;
        boolean dowrites = true;
        for (i = 1; i < (n - 1); i++) {
            int gridPointIndex = ((Integer) (pointVectorPtr.vector_at(i))).intValue();
            int z = gridPointIndex / area;
            int index2d = gridPointIndex % area;
            int y = index2d / width;
            int x = index2d % width;
            if (points_unaligned[x][y][z] != GRID_POINT_EMPTY) {
                dowrites = false;
            }
        }
        for (i = 1; i < (n - 1); i++) {
            int gridPointIndex = ((Integer) (pointVectorPtr.vector_at(i))).intValue();
            int z = gridPointIndex / area;
            int index2d = gridPointIndex % area;
            int y = index2d / width;
            int x = index2d % width;
            int[] array = points_unaligned[x][y];
            if (dowrites) array[z] = GRID_POINT_FULL;
        }
        return !dowrites;
    }

    public int getPointIndex(int x, int y, int z) {
        return ((z * height) + y) * width + x;
    }

    public void print() {
        int width = this.width;
        int height = this.height;
        int depth = this.depth;
        for (int z = 0; z < depth; z++) {
            System.out.println("[z =" + z + "]");
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    String str = String.valueOf(points_unaligned[x][y][z]);
                    for (int sp = 0; sp < (4 - str.length()); sp++) System.out.print(" ");
                    System.out.print(str);
                }
                System.out.println("");
            }
            System.out.println("");
        }
    }
}
