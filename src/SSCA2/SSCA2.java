package SSCA2;

import Yada.java.Barrier;

public class SSCA2 extends Thread {

    private static final boolean ENABLE_KERNEL1 = true;

    private static final boolean ENABLE_KERNEL2 = false;

    private static final boolean USE_PARALLEL_DATA_GENERATION = true;

    private static final boolean ENABLE_KERNEL3 = false;

    private static final boolean ENABLE_KERNEL4 = false;

    GraphSDG SDGdata;

    /**
   * The graph data structure for this benchmark - see defs.h
   **/
    Graph G;

    /**
   *
   */
    ComputeGraph computeGraphArgs;

    /**
   * thread id
   **/
    int threadid;

    /**
   * Total number of threads
   **/
    int numThread;

    /**
   * Global Arguments 
   **/
    Globals glb;

    /**
   *  Gen scalable data
   **/
    GenScalData gsd;

    /**
   **
   **/
    GetStartLists getStartListsArg;

    Alg_Radix_Smp radixsort;

    public SSCA2(int myId, int numThread, Globals glb, ComputeGraph computeGraphArgs, GenScalData gsd, GetStartLists getStartListsArg, Alg_Radix_Smp radixsort) {
        this.threadid = myId;
        this.numThread = numThread;
        this.glb = glb;
        this.computeGraphArgs = computeGraphArgs;
        this.G = computeGraphArgs.GPtr;
        this.SDGdata = computeGraphArgs.SDGdataPtr;
        this.gsd = gsd;
        this.getStartListsArg = getStartListsArg;
        this.radixsort = radixsort;
    }

    public void run() {
        if (USE_PARALLEL_DATA_GENERATION) {
            Barrier.enterBarrier();
            GenScalData.genScalData(threadid, numThread, glb, SDGdata, gsd, radixsort);
            Barrier.enterBarrier();
        }
        if (ENABLE_KERNEL1) {
            Barrier.enterBarrier();
            ComputeGraph.computeGraph(threadid, numThread, glb, computeGraphArgs);
            Barrier.enterBarrier();
        }
        if (ENABLE_KERNEL2) { // this branch is not executed, soot compiler does not analyze it
            Barrier.enterBarrier();
            GetStartLists.getStartLists(threadid, numThread, glb, getStartListsArg);
            Barrier.enterBarrier();
        }
    }

    public static void main(String[] args) {

        GraphSDG SDGdata = new GraphSDG();
        Graph G = new Graph();
        ComputeGraph computeGraphArgs = new ComputeGraph();
        long starttime;
        long stoptime;
        computeGraphArgs.GPtr = G;
        computeGraphArgs.SDGdataPtr = SDGdata;
        System.out.println("\nHPCS SSCA #2 Graph Analysis Executable Specification:");
        System.out.println("\nRunning...\n\n");
        Globals glb = new Globals();
        GetUserParameters gup = new GetUserParameters(glb);
        gup.getUserParameters(args, glb);
        System.out.println("Number of processors:       " + glb.THREADS);
        System.out.println("Problem Scale:              " + glb.SCALE);
        System.out.println("Max parallel edges:         " + glb.MAX_PARAL_EDGES);
        System.out.println("Percent int weights:        " + glb.PERC_INT_WEIGHTS);
        System.out.println("Probability unidirectional: " + glb.PROB_UNIDIRECTIONAL);
        System.out.println("Probability inter-clique:   " + glb.PROB_INTERCL_EDGES);
        System.out.println("Subgraph edge length:       " + glb.SUBGR_EDGE_LENGTH);
        System.out.println("Kernel 3 data structure:    " + glb.K3_DS);
       
        SSCA2[] ssca = new SSCA2[glb.THREADS];
        int nthreads = glb.THREADS;
        GenScalData gsd = new GenScalData();
        Alg_Radix_Smp radixsort = new Alg_Radix_Smp();
        GetStartLists getStartListsArg = new GetStartLists();
        getStartListsArg.GPtr = G;       
        Barrier.setBarrier(nthreads);
        for (int i = 1; i < nthreads; i++) {
            ssca[i] = new SSCA2(i, nthreads, glb, computeGraphArgs, gsd, getStartListsArg, radixsort);
        }
        starttime = System.currentTimeMillis();
        for (int i = 1; i < nthreads; i++) {
            ssca[i].start();
        }
        System.out.println("\nScalable Data Generator - genScalData() beginning execution...\n");
        
        if (USE_PARALLEL_DATA_GENERATION) {
            parallel_work_genScalData(nthreads, glb, SDGdata, gsd, radixsort);
        } else {
            GenScalData.genScalData_seq(glb, SDGdata, gsd, radixsort);
        }
        

        if (ENABLE_KERNEL1) {
            System.out.println("\nKernel 1 - computeGraph() beginning execution...");
            starttime = System.currentTimeMillis();
            parallel_work_computeGraph(nthreads, glb, computeGraphArgs);
            stoptime = System.currentTimeMillis();
            System.out.println("\n\tcomputeGraph() completed execution.\n");
            System.out.println("TIME=" + (stoptime - starttime));
        }
        if (ENABLE_KERNEL2) {
            getStartListsArg.GPtr = G;
            getStartListsArg.maxIntWtListPtr = null;
            getStartListsArg.maxIntWtListSize = 0;
            getStartListsArg.soughtStrWtListPtr = null;
            getStartListsArg.soughtStrWtListSize = 0;
            System.out.println("\nKernel 2 - getStartLists() beginning execution...\n");
            parallel_work_getStartLists(nthreads, glb, getStartListsArg);
            System.out.println("\n\tgetStartLists() completed execution.\n");
        }
        if (ENABLE_KERNEL3) {
            if (!ENABLE_KERNEL2) {
                throw new RuntimeException("kernel3 requires kernel2");
            }
        }
        if (ENABLE_KERNEL3) {
            VList[] intWtVList = null;
            VList[] strWtVList = null;
            System.out.println("\nKernel 3 - FindSubGraphs() beginning execution...\n");
            if (glb.K3_DS == 0) {
            } else if (glb.K3_DS == 1) {
            } else if (glb.K3_DS == 2) {
            } else {
                ;
            }
            System.out.println("\n\tFindSubGraphs() completed execution.\n");
        }
        if (ENABLE_KERNEL4) {
            System.out.println("\nKernel 4 - cutClusters() beginning execution...\n");
            parallel_work_cutClusters(G);
            System.out.println("\n\tcutClusters() completed execution.\n");
        }
        
        for (int i = 1; i < nthreads; i++) {
            try {
				ssca[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        stoptime = System.currentTimeMillis();
        System.out.println("\n\tgenScalData() completed execution.");
        System.out.println("Time=" + (stoptime - starttime));
        System.exit(0);
    
    }

    public static void parallel_work_genScalData(int numThread, Globals glb, GraphSDG SDGdata, GenScalData gsd, Alg_Radix_Smp radixsort) {
        Barrier.enterBarrier();
        GenScalData.genScalData(0, numThread, glb, SDGdata, gsd, radixsort);
        Barrier.enterBarrier();
    }

    public static void parallel_work_computeGraph(int numThread, Globals glb, ComputeGraph computeGraphArgs) {
        Barrier.enterBarrier();
        ComputeGraph.computeGraph(0, numThread, glb, computeGraphArgs);
        Barrier.enterBarrier();
    }

    public static void parallel_work_getStartLists(int numThread, Globals glb, GetStartLists getStartListsArg) {
        Barrier.enterBarrier();
        GetStartLists.getStartLists(0, numThread, glb, getStartListsArg);
        Barrier.enterBarrier();
    }

    public static void parallel_work_cutClusters(Graph G) {
        Barrier.enterBarrier();
        Barrier.enterBarrier();
    }
}
