package Yada.java;

import java.util.ArrayList;
import java.util.List;

public class yada extends Thread {

    String global_inputPrefix;

    int global_numThread;

    double global_angleConstraint;

    mesh global_meshPtr;

    heap global_workHeapPtr;

    int global_totalNumAdded;

    int global_numProcess;

    global_arg globalvar;

    public yada() {
        global_inputPrefix = "";
        global_numThread = 1;
        global_angleConstraint = 20.0;
        global_totalNumAdded = 0;
        global_numProcess = 0;
    }

    public yada(mesh meshptr, heap heapptr, double angle, global_arg g) {
        global_meshPtr = meshptr;
        global_workHeapPtr = heapptr;
        global_angleConstraint = angle;
        globalvar = g;
    }

    public static void displayUsage() {
        System.out.println("Usage: Yada [options]");
        System.out.println("Options:                              (defaults)");
        System.out.println("    a <FLT>   Min [a]ngle constraint  (20.0)");
        System.out.println("    i <STR>   [i]nput name prefix     (\"\")");
        System.out.println("    t <UINT>  Number of [t]hreads     (1L)");
        System.exit(1);
    }

    public void parseArgs(String argv[]) {
        for (int index = 0; index < argv.length; index++) {
            if (argv[index].equals("-a")) {
                index++;
                global_angleConstraint = Double.parseDouble(argv[index]);
            } else if (argv[index].equals("-i")) {
                index++;
                global_inputPrefix = argv[index];
            } else if (argv[index].equals("-t")) {
                index++;
                global_numThread = Integer.parseInt(argv[index]);
            } else {
                displayUsage();
                System.exit(-1);
            }
        }
    }

    public static int initializeWork(heap workHeapPtr, mesh meshPtr) {
        Random randomPtr = new Random();
        randomPtr.random_seed(0);
        meshPtr.mesh_shuffleBad(randomPtr);
        int numBad = 0;
        while (true) {
            element elementPtr = meshPtr.mesh_getBad();
            if (elementPtr == null) {
                break;
            }
            numBad++;
            boolean status = workHeapPtr.heap_insert(elementPtr);
            yada.Assert(status);
            elementPtr.element_setIsReferenced(true);
        }
        return numBad;
    }

    public static void Assert(boolean status) {
        if (!status) {
            System.out.println("Failed assert");
            int[] x = new int[1];
            x[0] = 3 / 0;
        }
    }

    public void process() {
        heap workHeapPtr = global_workHeapPtr;
        mesh meshPtr = global_meshPtr;
        int totalNumAdded = 0;
        int numProcess = 0;
        region regionPtr = new region();
        while (true) {
            element elementPtr;
            synchronized (common.G.lock) {
                elementPtr = (element) workHeapPtr.heap_remove();
            }
            if (elementPtr == null) {
                break;
            }
            boolean isGarbage;
            synchronized (common.G.lock) {
                isGarbage = elementPtr.element_isGarbage();
            }
            if (isGarbage) {
                continue;
            }
            int numAdded;
            synchronized (common.G.lock) {
                regionPtr.region_clearBad();
                numAdded = regionPtr.TMregion_refine(elementPtr, meshPtr, global_angleConstraint);
            }
            synchronized (common.G.lock) {
                elementPtr.element_setIsReferenced(false);
                isGarbage = elementPtr.element_isGarbage();
            }
            totalNumAdded += numAdded;
            synchronized (common.G.lock) {
                regionPtr.region_transferBad(workHeapPtr);
            }
            numProcess++;
        }
        synchronized (common.G.lock) {
            globalvar.global_totalNumAdded = globalvar.global_totalNumAdded + totalNumAdded;
            globalvar.global_numProcess = globalvar.global_numProcess + numProcess;
        }
    }

    public void run() {
        Barrier.enterBarrier();
        process();
        Barrier.enterBarrier();
    }

    public static void main(String[] argv) throws Exception {
        yada y = new yada();
        global_arg g = new global_arg();
        y.globalvar = g;
        y.parseArgs(argv);
        List yadas = new ArrayList();
        yadas.add(0, null);
        y.global_meshPtr = new mesh(y.global_angleConstraint);
        System.out.println("Angle constraint = " + y.global_angleConstraint);
        System.out.println("Reading input... ");
        int initNumElement = y.global_meshPtr.mesh_read(y.global_inputPrefix);
        System.out.println("done.");
        y.global_workHeapPtr = new heap(1);
        Barrier.setBarrier(y.global_numThread);
        long start = System.currentTimeMillis();
        for (int i = 1; i < y.global_numThread; i++) {
            yada ychild = new yada(y.global_meshPtr, y.global_workHeapPtr, y.global_angleConstraint, g);
            yadas.add(i, ychild);
            ychild.start();
        }
        int initNumBadElement = initializeWork(y.global_workHeapPtr, y.global_meshPtr);
        System.out.println("Initial number of mesh elements = " + initNumElement);
        System.out.println("Initial number of bad elements  = " + initNumBadElement);
        System.out.println("Starting triangulation...");
        y.run();
        for (int i = 1; i < y.global_numThread; i++) {
            yada ychild = (yada) yadas.get(i);
            ychild.join();
        }
        long stop = System.currentTimeMillis();
        System.out.println(" done.");
        System.out.println("Elapsed time                    = " + (stop - start));
        int finalNumElement = initNumElement + y.globalvar.global_totalNumAdded;
        System.out.println("Final mesh size                 = " + finalNumElement);
        System.out.println("Number of elements processed    = " + y.globalvar.global_numProcess);
        boolean check = y.global_meshPtr.mesh_check(finalNumElement);
        if (!check) System.out.println("Mesh is invalid");
    }
}
