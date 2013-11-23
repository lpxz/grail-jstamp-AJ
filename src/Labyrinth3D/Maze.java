package Labyrinth3D;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.StringTokenizer;
import common.Pair;

public class Maze {

    private static int GRID_POINT_FULL = -2;

    private static int GRID_POINT_EMPTY = -1;

    Grid gridPtr;

    Queue_t workQueuePtr;

    Vector_t wallVectorPtr;

    Vector_t srcVectorPtr;

    Vector_t dstVectorPtr;

    public Maze() {
    }

    public static Maze alloc() {
        Maze mazePtr = new Maze();
        mazePtr.gridPtr = null;
        mazePtr.workQueuePtr = Queue_t.queue_alloc(1024);
        mazePtr.wallVectorPtr = Vector_t.vector_alloc(1);
        mazePtr.srcVectorPtr = Vector_t.vector_alloc(1);
        mazePtr.dstVectorPtr = Vector_t.vector_alloc(1);
        return mazePtr;
    }

    public static void free(Maze m) {
        m = null;
    }

    private void addToGrid(Grid gridPtr, Vector_t vectorPtr, String type) {
        int i;
        int n = vectorPtr.vector_getSize();
        for (i = 0; i < n; i++) {
            Coordinate coordinatePtr = (Coordinate) vectorPtr.vector_at(i);
            if (!gridPtr.isPointValid(coordinatePtr.x, coordinatePtr.y, coordinatePtr.z)) {
                System.out.println("Error: " + type + " (" + coordinatePtr.x + ", " + coordinatePtr.y + ", " + coordinatePtr.z);
                System.exit(1);
            }
        }
        gridPtr.addPath(vectorPtr);
    }

    public int readMaze(String inputFileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        int lineNumber = 0;
        int height = -1;
        int width = -1;
        int depth = -1;
        boolean isParseError = false;
        List_t workListPtr = List_t.alloc(1);
        String line;
        while ((line = br.readLine()) != null) {
            String code;
            int[] xy = new int[6];
            int numToken = 0;
            StringTokenizer tok = new StringTokenizer(line);
            if ((numToken = tok.countTokens()) < 1) {
                continue;
            }
            code = tok.nextToken();
            if (code.equals("#")) {
                continue;
            }
            for (int i = 0; i < numToken - 1; i++) {
                xy[i] = Integer.parseInt(tok.nextToken());
            }
            if (code.equals("d")) {
                if (numToken != 4) {
                    isParseError = true;
                } else {
                    width = xy[0];
                    height = xy[1];
                    depth = xy[2];
                    if (width < 1 || height < 1 || depth < 1) isParseError = true;
                }
            } else if (code.equals("p")) {
                if (numToken != 7) {
                    isParseError = true;
                } else {
                    Coordinate srcPtr = Coordinate.alloc(xy[0], xy[1], xy[2]);
                    Coordinate dstPtr = Coordinate.alloc(xy[3], xy[4], xy[5]);
                    if (Coordinate.isEqual(srcPtr, dstPtr)) {
                        isParseError = true;
                    } else {
                        Pair coordinatePairPtr = Pair.alloc(srcPtr, dstPtr);
                        boolean status = workListPtr.insert(coordinatePairPtr);
                        srcVectorPtr.vector_pushBack(srcPtr);
                        dstVectorPtr.vector_pushBack(dstPtr);
                    }
                }
            } else if (code.equals("w")) {
                if (numToken != 4) {
                    isParseError = true;
                } else {
                    Coordinate wallPtr = Coordinate.alloc(xy[0], xy[1], xy[2]);
                    wallVectorPtr.vector_pushBack(wallPtr);
                }
            } else {
                isParseError = true;
            }
            if (isParseError) {
                System.out.println("Error: line " + lineNumber + " of " + inputFileName + "invalid");
                System.exit(1);
            }
        }
        br.close();
        if (width < 1 || height < 1 || depth < 1) {
            System.out.println("Error : Invalid dimensions ( " + width + ", " + height + ", " + depth + ")");
            System.exit(1);
        }
        Grid gridPtr = Grid.alloc(width, height, depth);
        this.gridPtr = gridPtr;
        addToGrid(gridPtr, wallVectorPtr, "wall");
        addToGrid(gridPtr, srcVectorPtr, "source");
        addToGrid(gridPtr, dstVectorPtr, "destination");
        System.out.println("Maze dimensions = " + width + " x " + height + " x " + depth);
        System.out.println("Paths to route  = " + workListPtr.getSize());
        List_Iter it = new List_Iter();
        it.reset(workListPtr);
        while (it.hasNext(workListPtr)) {
            Pair coordinatePairPtr = (Pair) it.next(workListPtr);
            workQueuePtr.queue_push(coordinatePairPtr);
        }
        List_t.free(workListPtr);
        return srcVectorPtr.vector_getSize();
    }

    public boolean checkPaths(List_t pathVectorListPtr, boolean doPrintPaths) {
        int i;
        Grid testGridPtr = Grid.alloc(gridPtr.width, gridPtr.height, gridPtr.depth);
        testGridPtr.addPath(wallVectorPtr);
        int numSrc = srcVectorPtr.vector_getSize();
        for (i = 0; i < numSrc; i++) {
            Coordinate srcPtr = (Coordinate) srcVectorPtr.vector_at(i);
            testGridPtr.setPoint(srcPtr.x, srcPtr.y, srcPtr.z, 0);
        }
        int numdst = dstVectorPtr.vector_getSize();
        for (i = 0; i < numdst; i++) {
            Coordinate dstPtr = (Coordinate) dstVectorPtr.vector_at(i);
            testGridPtr.setPoint(dstPtr.x, dstPtr.y, dstPtr.z, 0);
        }
        int id = 0;
        List_Iter it = new List_Iter();
        it.reset(pathVectorListPtr);
        int height = gridPtr.height;
        int width = gridPtr.width;
        int area = height * width;
        while (it.hasNext(pathVectorListPtr)) {
            Vector_t pathVectorPtr = (Vector_t) it.next(pathVectorListPtr);
            int numPath = pathVectorPtr.vector_getSize();
            for (i = 0; i < numPath; i++) {
                id++;
                Vector_t pointVectorPtr = (Vector_t) pathVectorPtr.vector_at(i);
                int prevGridPointIndex = ((Integer) pointVectorPtr.vector_at(0)).intValue();
                int z = prevGridPointIndex / area;
                int index2d = prevGridPointIndex % area;
                int y = index2d / width;
                int x = index2d % width;
                if (testGridPtr.getPoint(x, y, z) != 0) {
                    return false;
                }
                Coordinate prevCoordinate = new Coordinate();
                prevCoordinate.x = x;
                prevCoordinate.y = y;
                prevCoordinate.z = z;
                int numPoint = pointVectorPtr.vector_getSize();
                int j;
                for (j = 1; j < (numPoint - 1); j++) {
                    int currGridPointIndex = ((Integer) pointVectorPtr.vector_at(j)).intValue();
                    Coordinate currCoordinate = new Coordinate();
                    z = currGridPointIndex / area;
                    index2d = currGridPointIndex % area;
                    y = index2d / width;
                    x = index2d % width;
                    currCoordinate.x = x;
                    currCoordinate.y = y;
                    currCoordinate.z = z;
                    if (!Coordinate.areAdjacent(currCoordinate, prevCoordinate)) {
                        System.out.println("you there?");
                        return false;
                    }
                    prevCoordinate = currCoordinate;
                    int xx = currCoordinate.x;
                    int yy = currCoordinate.y;
                    int zz = currCoordinate.z;
                    if (testGridPtr.getPoint(xx, yy, zz) != GRID_POINT_EMPTY) {
                        return false;
                    } else {
                        testGridPtr.setPoint(xx, yy, zz, id);
                    }
                }
                int lastGridPointIndex = ((Integer) pointVectorPtr.vector_at(j)).intValue();
                z = lastGridPointIndex / area;
                index2d = lastGridPointIndex % area;
                y = index2d / width;
                x = index2d % width;
                if (testGridPtr.getPoint(x, y, z) != 0) {
                    return false;
                }
            }
        }
        if (doPrintPaths) {
            System.out.println("\nRouted Maze:");
            testGridPtr.print();
        }
        return true;
    }
}
