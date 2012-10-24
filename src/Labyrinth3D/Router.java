package Labyrinth3D;

import common.Pair;

public class Router {
	/*atomicset(R)*/

    public static int MOMENTUM_ZERO = 0;

    public static int MOMENTUM_POSX = 1;

    public static int MOMENTUM_POSY = 2;

    public static int MOMENTUM_POSZ = 3;

    public static int MOMENTUM_NEGX = 4;

    public static int MOMENTUM_NEGY = 5;

    public static int MOMENTUM_NEGZ = 6;

    public static int GRID_POINT_FULL = -2;

    public static int GRID_POINT_EMPTY = -1;

/*atomic(R)*/    public int xCost;

/*atomic(R)*/    public int yCost;

/*atomic(R)*/    public int zCost;

 /*atomic(R)*/   public int bendCost;

    public static Point MOVE_POSX;

    public static Point MOVE_POSY;

    public static Point MOVE_POSZ;

    public static Point MOVE_NEGX;

    public static Point MOVE_NEGY;

    public static Point MOVE_NEGZ;

    public Router() {
    }

    public static Router alloc(int xCost, int yCost, int zCost, int bendCost) {
        Router routerPtr = new Router();
        routerPtr.MOVE_POSX = new Point(1, 0, 0, 0, MOMENTUM_POSX);
        routerPtr.MOVE_POSY = new Point(0, 1, 0, 0, MOMENTUM_POSY);
        routerPtr.MOVE_POSZ = new Point(0, 0, 1, 0, MOMENTUM_POSZ);
        routerPtr.MOVE_NEGX = new Point(-1, 0, 0, 0, MOMENTUM_NEGX);
        routerPtr.MOVE_NEGY = new Point(0, -1, 0, 0, MOMENTUM_NEGY);
        routerPtr.MOVE_NEGZ = new Point(0, 0, -1, 0, MOMENTUM_NEGZ);
        routerPtr.xCost = xCost;
        routerPtr.yCost = yCost;
        routerPtr.zCost = zCost;
        routerPtr.bendCost = bendCost;
        return routerPtr;
    }

    private void PexpandToNeighbor(Grid myGridPtr, int x, int y, int z, int value, Queue_Int queuePtr) {
        if (myGridPtr.isPointValid(x, y, z)) {
            int neighborValue = myGridPtr.points_unaligned[x][y][z];
            if (neighborValue == GRID_POINT_EMPTY) {
                int neighborGridPointIndex = myGridPtr.getPointIndex(x, y, z);
                myGridPtr.points_unaligned[x][y][z] = value;
                queuePtr.queue_push(neighborGridPointIndex);
            } else if (neighborValue != GRID_POINT_FULL) {
                if (value < neighborValue) {
                    int neighborGridPointIndex = myGridPtr.getPointIndex(x, y, z);
                    myGridPtr.points_unaligned[x][y][z] = value;
                    queuePtr.queue_push(neighborGridPointIndex);
                }
            }
        }
    }

    public boolean PdoExpansion(Router routerPtr, Grid myGridPtr, Queue_Int queuePtr, Coordinate srcPtr, Coordinate dstPtr) {
        int xCost = routerPtr.xCost;
        int yCost = routerPtr.yCost;
        int zCost = routerPtr.zCost;
        queuePtr.queue_clear();
        int srcGridPointIndex = myGridPtr.getPointIndex(srcPtr.x, srcPtr.y, srcPtr.z);
        queuePtr.queue_push(srcGridPointIndex);
        myGridPtr.setPoint(srcPtr.x, srcPtr.y, srcPtr.z, 0);
        myGridPtr.setPoint(dstPtr.x, dstPtr.y, dstPtr.z, GRID_POINT_EMPTY);
        int dstGridPointIndex = myGridPtr.getPointIndex(dstPtr.x, dstPtr.y, dstPtr.z);
        boolean isPathFound = false;
        int height = myGridPtr.height;
        int width = myGridPtr.width;
        int area = height * width;
        while (!queuePtr.queue_isEmpty()) {
            int gridPointIndex = queuePtr.queue_pop();
            if (gridPointIndex == dstGridPointIndex) {
                isPathFound = true;
                break;
            }
            int z = gridPointIndex / area;
            int index2d = gridPointIndex % area;
            int y = index2d / width;
            int x = index2d % width;
            int value = myGridPtr.points_unaligned[x][y][z];
            PexpandToNeighbor(myGridPtr, x + 1, y, z, (value + xCost), queuePtr);
            PexpandToNeighbor(myGridPtr, x - 1, y, z, (value + xCost), queuePtr);
            PexpandToNeighbor(myGridPtr, x, y + 1, z, (value + yCost), queuePtr);
            PexpandToNeighbor(myGridPtr, x, y - 1, z, (value + yCost), queuePtr);
            PexpandToNeighbor(myGridPtr, x, y, z + 1, (value + zCost), queuePtr);
            PexpandToNeighbor(myGridPtr, x, y, z - 1, (value + zCost), queuePtr);
        }
        return isPathFound;
    }

    private void traceToNeighbor(Grid myGridPtr, Point currPtr, Point movePtr, boolean useMomentum, int bendCost, Point nextPtr) {
        int x = currPtr.x + movePtr.x;
        int y = currPtr.y + movePtr.y;
        int z = currPtr.z + movePtr.z;
        if (myGridPtr.isPointValid(x, y, z) && !myGridPtr.isPointEmpty(x, y, z) && !myGridPtr.isPointFull(x, y, z)) {
            int value = myGridPtr.getPoint(x, y, z);
            int b = 0;
            if (useMomentum && (currPtr.momentum != movePtr.momentum)) {
                b = bendCost;
            }
            if ((value + b) <= nextPtr.value) {
                nextPtr.x = x;
                nextPtr.y = y;
                nextPtr.z = z;
                nextPtr.value = value;
                nextPtr.momentum = movePtr.momentum;
            }
        }
    }

    private Vector_t PdoTraceback(Grid myGridPtr, Coordinate dstPtr, int bendCost) {
        Vector_t pointVectorPtr = Vector_t.vector_alloc(1);
        Point next = new Point();
        next.x = dstPtr.x;
        next.y = dstPtr.y;
        next.z = dstPtr.z;
        next.value = myGridPtr.getPoint(next.x, next.y, next.z);
        next.momentum = MOMENTUM_ZERO;
        while (true) {
            int gridPointIndex = myGridPtr.getPointIndex(next.x, next.y, next.z);
            pointVectorPtr.vector_pushBack(new Integer(gridPointIndex));
            myGridPtr.setPoint(next.x, next.y, next.z, GRID_POINT_FULL);
            if (next.value == 0) {
                break;
            }
            Point curr = new Point();
            curr.x = next.x;
            curr.y = next.y;
            curr.z = next.z;
            curr.value = next.value;
            curr.momentum = next.momentum;
            traceToNeighbor(myGridPtr, curr, MOVE_POSX, true, bendCost, next);
            traceToNeighbor(myGridPtr, curr, MOVE_POSY, true, bendCost, next);
            traceToNeighbor(myGridPtr, curr, MOVE_POSZ, true, bendCost, next);
            traceToNeighbor(myGridPtr, curr, MOVE_NEGX, true, bendCost, next);
            traceToNeighbor(myGridPtr, curr, MOVE_NEGY, true, bendCost, next);
            traceToNeighbor(myGridPtr, curr, MOVE_NEGZ, true, bendCost, next);
            if ((curr.x == next.x) && (curr.y == next.y) && (curr.z == next.z)) {
                next.value = curr.value;
                traceToNeighbor(myGridPtr, curr, MOVE_POSX, false, bendCost, next);
                traceToNeighbor(myGridPtr, curr, MOVE_POSY, false, bendCost, next);
                traceToNeighbor(myGridPtr, curr, MOVE_POSZ, false, bendCost, next);
                traceToNeighbor(myGridPtr, curr, MOVE_NEGX, false, bendCost, next);
                traceToNeighbor(myGridPtr, curr, MOVE_NEGY, false, bendCost, next);
                traceToNeighbor(myGridPtr, curr, MOVE_NEGZ, false, bendCost, next);
                if ((curr.x == next.x) && (curr.y == next.y) && (curr.z == next.z)) {
                    System.out.println("Dead");
                    return null;
                }
            }
        }
        return pointVectorPtr;
    }

    public static void solve(Object argPtr) {
        Solve_Arg routerArgPtr = (Solve_Arg) argPtr;
        Router routerPtr = routerArgPtr.routerPtr;
        Maze mazePtr = routerArgPtr.mazePtr;
        Vector_t myPathVectorPtr = Vector_t.vector_alloc(1);
        Queue_t workQueuePtr = mazePtr.workQueuePtr;
        Grid gridPtr = mazePtr.gridPtr;
        Grid myGridPtr = Grid.scratchalloc(gridPtr.width, gridPtr.height, gridPtr.depth);
        int bendCost = routerPtr.bendCost;
        Queue_Int myExpansionQueuePtr = Queue_Int.queue_alloc(-1);
        while (true) {
            Pair coordinatePairPtr;
            synchronized (common.G.lock) {
                if (workQueuePtr.queue_isEmpty()) {
                    coordinatePairPtr = null;
                } else {
                    coordinatePairPtr = (Pair) workQueuePtr.queue_pop();
                }
            }
            if (coordinatePairPtr == null) {
                break;
            }
            Coordinate srcPtr = (Coordinate) coordinatePairPtr.first;
            Coordinate dstPtr = (Coordinate) coordinatePairPtr.second;
            boolean success = false;
            Vector_t pointVectorPtr = null;
            boolean retry = true;
            while (retry) {
                retry = false;
                synchronized (common.G.lock) {
                    Grid.copy(myGridPtr, gridPtr);
                    if (routerPtr.PdoExpansion(routerPtr, myGridPtr, myExpansionQueuePtr, srcPtr, dstPtr)) {
                        pointVectorPtr = routerPtr.PdoTraceback(myGridPtr, dstPtr, bendCost);
                        if (pointVectorPtr != null) {
                            if (gridPtr.TM_addPath(pointVectorPtr)) {
                                pointVectorPtr = null;
                                retry = true;
                            } else success = true;
                        }
                    }
                }
            }
            if (success) {
                boolean status = myPathVectorPtr.vector_pushBack(pointVectorPtr);
            }
        }
        List_t pathVectorListPtr = routerArgPtr.pathVectorListPtr;
        synchronized (common.G.lock) {
            pathVectorListPtr.insert(myPathVectorPtr);
        }
        myGridPtr = null;
        myExpansionQueuePtr = null;
    }
}
