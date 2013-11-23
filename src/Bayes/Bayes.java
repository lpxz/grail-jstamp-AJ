package Bayes;

import Yada.java.Barrier;

public class Bayes extends Thread {

    private static final boolean SIMULATOR = false;

    private static final int PARAM_EDGE = 101;

    private static final int PARAM_INSERT = 105;

    private static final int PARAM_NUMBER = 110;

    private static final int PARAM_PERCENT = 112;

    private static final int PARAM_RECORD = 114;

    private static final int PARAM_SEED = 115;

    private static final int PARAM_THREAD = 116;

    private static final int PARAM_VAR = 118;

    private static final int PARAM_DEFAULT_EDGE = -1;

    private static final int PARAM_DEFAULT_INSERT = 1;

    private static final float PARAM_DEFAULT_QUALITY = 1.0f;

    private static final int PARAM_DEFAULT_NUMBER = 4;

    private static final int PARAM_DEFAULT_PERCENT = 10;

    private static final int PARAM_DEFAULT_RECORD = 4096;

    private static final int PARAM_DEFAULT_SEED = 1;

    private static final int PARAM_DEFAULT_THREAD = 1;

    private static final int PARAM_DEFAULT_VAR = 32;

    public int[] global_params;

    public int global_maxNumEdgeLearned;

    public int global_insertPenalty;

    public float global_operationQualityFactor;

    int numThread;

    int myId;

    Learner learnerPtr;

    public Bayes() {
        global_params = new int[256];
        global_maxNumEdgeLearned = PARAM_DEFAULT_EDGE;
        global_insertPenalty = PARAM_DEFAULT_INSERT;
        global_operationQualityFactor = PARAM_DEFAULT_QUALITY;
    }

    public Bayes(int numThread, int myId, Learner learnerPtr) {
        this.numThread = numThread;
        this.myId = myId;
        this.learnerPtr = learnerPtr;
    }

    public void displayUsage() {
        System.out.println("Usage: ./Bayes.bin [options]");
        System.out.println("    e Max [e]dges learned per variable  ");
        System.out.println("    i Edge [i]nsert penalty             ");
        System.out.println("    n Max [n]umber of parents           ");
        System.out.println("    p [p]ercent chance of parent        ");
        System.out.println("    q Operation [q]uality factor        ");
        System.out.println("    r Number of [r]ecords               ");
        System.out.println("    s Random [s]eed                     ");
        System.out.println("    t Number of [t]hreads               ");
        System.out.println("    v Number of [v]ariables             ");
        System.exit(1);
    }

    public void setDefaultParams() {
        global_params[PARAM_EDGE] = PARAM_DEFAULT_EDGE;
        global_params[PARAM_INSERT] = PARAM_DEFAULT_INSERT;
        global_params[PARAM_NUMBER] = PARAM_DEFAULT_NUMBER;
        global_params[PARAM_PERCENT] = PARAM_DEFAULT_PERCENT;
        global_params[PARAM_RECORD] = PARAM_DEFAULT_RECORD;
        global_params[PARAM_SEED] = PARAM_DEFAULT_SEED;
        global_params[PARAM_THREAD] = PARAM_DEFAULT_THREAD;
        global_params[PARAM_VAR] = PARAM_DEFAULT_VAR;
    }

    public static void parseArgs(String[] args, Bayes b) {
        int i = 0;
        String arg;
        b.setDefaultParams();
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("-e")) {
                if (i < args.length) {
                    b.global_params[PARAM_EDGE] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-i")) {
                if (i < args.length) {
                    b.global_params[PARAM_INSERT] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-n")) {
                if (i < args.length) {
                    b.global_params[PARAM_NUMBER] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-p")) {
                if (i < args.length) {
                    b.global_params[PARAM_PERCENT] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-r")) {
                if (i < args.length) {
                    b.global_params[PARAM_RECORD] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-s")) {
                if (i < args.length) {
                    b.global_params[PARAM_SEED] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-t")) {
                if (i < args.length) {
                    b.global_params[PARAM_THREAD] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-v")) {
                if (i < args.length) {
                    b.global_params[PARAM_VAR] = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-h")) {
                b.displayUsage();
            }
        }
        if (b.global_params[PARAM_THREAD] == 0) {
            b.displayUsage();
        }
    }

    public float score(Net netPtr, Adtree adtreePtr) {
        Data dataPtr = new Data(1, 1, null);
        Learner learnerPtr = new Learner(dataPtr, adtreePtr, 1, global_insertPenalty, global_maxNumEdgeLearned, global_operationQualityFactor);
        Net tmpNetPtr = learnerPtr.netPtr;
        learnerPtr.netPtr = netPtr;
        float score = learnerPtr.learner_score();
        learnerPtr.netPtr = tmpNetPtr;
        learnerPtr.learner_free();
        dataPtr.data_free();
        return score;
    }

    public void run() {
        Barrier.enterBarrier();
        {
            Learner.createTaskList(myId, numThread, learnerPtr);
        }
        Barrier.enterBarrier();
        Barrier.enterBarrier();
        Learner.learnStructure(myId, numThread, learnerPtr);
        Barrier.enterBarrier();
    }

    public static void main(String[] args) {
        Bayes b = new Bayes();
        Bayes.parseArgs(args, b);
        int numThread = b.global_params[PARAM_THREAD];
        int numVar = b.global_params[PARAM_VAR];
        int numRecord = b.global_params[PARAM_RECORD];
        int randomSeed = b.global_params[PARAM_SEED];
        int maxNumParent = b.global_params[PARAM_NUMBER];
        int percentParent = b.global_params[PARAM_PERCENT];
        b.global_insertPenalty = b.global_params[PARAM_INSERT];
        b.global_maxNumEdgeLearned = b.global_params[PARAM_EDGE];
        Barrier.setBarrier(numThread);
        Bayes[] binit = new Bayes[numThread];
        System.out.println("Number of threads          " + numThread);
        System.out.println("Random seed                " + randomSeed);
        System.out.println("Number of vars             " + numVar);
        System.out.println("Number of records          " + numRecord);
        System.out.println("Max num parents            " + maxNumParent);
        System.out.println("%% chance of parent        " + percentParent);
        System.out.println("Insert penalty             " + b.global_insertPenalty);
        System.out.println("Max num edge learned / var " + b.global_maxNumEdgeLearned);
        System.out.println("Operation quality factor   " + b.global_operationQualityFactor);
        System.out.print("Generating data... ");
        Random randomPtr = new Random();
        randomPtr.random_alloc();
        randomPtr.random_seed(randomSeed);
        Data dataPtr = new Data(numVar, numRecord, randomPtr);
        Net netPtr = dataPtr.data_generate(-1, maxNumParent, percentParent);
        System.out.println("done.");
        Adtree adtreePtr = new Adtree();
        System.out.print("Generating adtree... ");
        adtreePtr.adtree_make(dataPtr);
        dataPtr.data_free();
        System.out.println("done.");
        float actualScore = b.score(netPtr, adtreePtr);
        netPtr.net_free();
        Learner learnerPtr = new Learner(dataPtr, adtreePtr, numThread, b.global_insertPenalty, b.global_maxNumEdgeLearned, b.global_operationQualityFactor);
        System.out.print("Learning structure...");
        for (int i = 1; i < numThread; i++) {
            binit[i] = new Bayes(i, numThread, learnerPtr);
        }
        long start = System.currentTimeMillis();
        for (int i = 1; i < numThread; i++) {
            binit[i].start();
        }
        Barrier.enterBarrier();
        Learner.createTaskList(0, numThread, learnerPtr);
        Barrier.enterBarrier();
        Barrier.enterBarrier();
        Learner.learnStructure(0, numThread, learnerPtr);
        Barrier.enterBarrier();
        for (int i = 1; i < numThread; i++) {
            try {
                binit[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        boolean status = learnerPtr.netPtr.net_isCycle();
        if (SIMULATOR) {
            float learnScore = learnerPtr.learner_score();
            System.out.println("Learn score= " + (double) learnScore);
        }
        System.out.println("Actual score= " + (double) actualScore);
        long stop = System.currentTimeMillis();
        long diff = stop - start;
        System.out.println("TIME=" + diff);
        System.out.println("done.");
    }
}
