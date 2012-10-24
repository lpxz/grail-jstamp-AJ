package Labyrinth3D;

import Yada.java.Barrier;

public class Labyrinth extends Thread {

    static String global_inputFile;

    static boolean global_doPrint;

    int numThread;

    int bendCost;

    int xCost;

    int yCost;

    int zCost;

    int threadID;

    Solve_Arg routerArg;

    private void setDefaultParams() {
        global_inputFile = null;
        global_doPrint = false;
        bendCost = 1;
        xCost = 1;
        yCost = 1;
        zCost = 2;
        numThread = 1;
    }

    private void parseArg(String[] argv) {
        int i = 0;
        String arg;
        boolean opterr = false;
        setDefaultParams();
        while (i < argv.length) {
            if (argv[i].charAt(0) == '-') {
                arg = argv[i++];
                if (arg.equals("-b")) {
                    bendCost = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-x")) {
                    xCost = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-y")) {
                    yCost = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-z")) {
                    zCost = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-t")) {
                    numThread = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-i")) {
                    global_inputFile = argv[i++];
                } else if (arg.equals("-p")) {
                    global_doPrint = true;
                } else {
                    System.out.println("Non-option argument: " + argv[i]);
                    opterr = true;
                }
            }
        }
        if (opterr) {
            displayUsage();
            System.exit(1);
        }
    }

    public Labyrinth(String[] argv) {
        parseArg(argv);
    }

    public Labyrinth(int myID, Solve_Arg rArg) {
        threadID = myID;
        routerArg = rArg;
    }

    public void run() {
        Barrier.enterBarrier();
        Router.solve(routerArg);
        Barrier.enterBarrier();
    }

    public void displayUsage() {
        System.out.println("Usage: Labyrinth [options]");
        System.out.println("Options:");
        System.out.println("    b <INT>     bend cost");
        System.out.println("    i <FILE>    input file name");
        System.out.println("    p           print routed maze");
        System.out.println("    t <INT>     Number of threads");
        System.out.println("    x <INT>     x movement cost");
        System.out.println("    y <INT>     y movement cost");
        System.out.println("    z <INT>     z movement cost");
    }

    public static void main(String[] argv) throws Exception {
        Labyrinth labyrinth = new Labyrinth(argv);
       
        Maze mazePtr = Maze.alloc();
        int numPathToRoute = mazePtr.readMaze(labyrinth.global_inputFile);
        Router routerPtr = Router.alloc(labyrinth.xCost, labyrinth.yCost, labyrinth.zCost, labyrinth.bendCost);
        List_t pathVectorListPtr = List_t.alloc(0);
        Solve_Arg routerArg = new Solve_Arg(routerPtr, mazePtr, pathVectorListPtr);
        Labyrinth[] lb = new Labyrinth[labyrinth.numThread];
      //  Barrier.setBarrier(labyrinth.numThread);
        for (int i = 1; i < labyrinth.numThread; i++) {
            lb[i] = new Labyrinth(i, routerArg);
        }
        long start = System.currentTimeMillis();
        for (int i = 1; i < labyrinth.numThread; i++) {
            lb[i].start();
        }
       
        for (int i = 1; i < labyrinth.numThread; i++) {
            lb[i].join();
        }
        Barrier.enterBarrier();
        Router.solve(routerArg);
        Barrier.enterBarrier();
        

        long finish = System.currentTimeMillis();
        long diff = finish - start;
        System.out.println("TIME=" + diff);
        int numPathRouted = 0;
        List_Iter it = new List_Iter();
        it.reset(pathVectorListPtr);
        while (it.hasNext(pathVectorListPtr)) {
            Vector_t pathVectorPtr = (Vector_t) it.next(pathVectorListPtr);
            numPathRouted += pathVectorPtr.vector_getSize();
        }
        float elapsed = ((float) finish - (float) start) / 1000;
        System.out.println("Paths routed    = " + numPathRouted);
        System.out.println("Elapsed time    = " + elapsed);
        boolean stats = mazePtr.checkPaths(pathVectorListPtr, labyrinth.global_doPrint);
        if (!stats) {
            System.out.println("Verification not passed");
        } else System.out.println("Verification passed.");
        System.out.println("Finished");
    }
}
