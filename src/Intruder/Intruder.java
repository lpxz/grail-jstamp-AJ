package Intruder;

import Yada.java.Barrier;

public class Intruder extends Thread {

    private static final int PARAM_DEFAULT_ATTACK = 10;

    private static final int PARAM_DEFAULT_LENGTH = 16;

    private static final int PARAM_DEFAULT_NUM = (1 << 20);

    private static final int PARAM_DEFAULT_SEED = 1;

    private static final int PARAM_DEFAULT_THREAD = 1;

    private static final char PARAM_ATTACK = 'a';

    private static final char PARAM_LENGTH = 'l';

    private static final char PARAM_NUM = 'n';

    private static final char PARAM_SEED = 's';

    private static final char PARAM_THREAD = 't';

    int percentAttack;

    int maxDataLength;

    int numFlow;

    int randomSeed;

    int numThread;

    int threadID;

    Arg argument;

    public Intruder(String[] argv) {
        parseArg(argv);
    }

    public Intruder(int myID, Arg a) {
        argument = a;
        threadID = myID;
    }

    private void setDefaultParams() {
        percentAttack = PARAM_DEFAULT_ATTACK;
        maxDataLength = PARAM_DEFAULT_LENGTH;
        numFlow = PARAM_DEFAULT_NUM;
        randomSeed = PARAM_DEFAULT_SEED;
        numThread = PARAM_DEFAULT_THREAD;
    }

    private void displayUsage() {
        System.out.print("Usage: Intruder [options]\n");
        System.out.println("\nOptions:                            (defaults)\n");
        System.out.print("    a <UINT>   Percent [a]ttack     ");
        System.out.print("    l <UINT>   Max data [l]ength    ");
        System.out.print("    n <UINT>   [n]umber of flows    ");
        System.out.print("    s <UINT>   Random [s]eed        ");
        System.out.print("    t <UINT>   Number of [t]hreads  ");
        System.exit(1);
    }

    private void parseArg(String[] argv) {
        int i = 0;
        String arg;
        boolean opterr = false;
        setDefaultParams();
        while (i < argv.length) {
            if (argv[i].charAt(0) == '-') {
                arg = argv[i++];
                if (arg.equals("-a")) {
                    percentAttack = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-l")) {
                    maxDataLength = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-n")) {
                    numFlow = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-s")) {
                    randomSeed = Integer.parseInt(argv[i++]);
                } else if (arg.equals("-t")) {
                    numThread = Integer.parseInt(argv[i++]);
                } else {
                    System.out.println("Non-option argument: " + argv[i]);
                    opterr = true;
                }
            }
        }
        if (opterr) {
            displayUsage();
        }
    }

    public void processPackets(Arg argPtr) {
        Stream streamPtr = argPtr.streamPtr;
        Decoder decoderPtr = argPtr.decoderPtr;
        Vector_t[] errorVectors = argPtr.errorVectors;
        Detector detectorPtr = new Detector();
        detectorPtr.addPreprocessor(2);
        Vector_t errorVectorPtr = errorVectors[threadID];
        while (true) {
            Packet packetPtr;
            synchronized (common.G.lock) {
                packetPtr = streamPtr.getPacket();
            }
            if (packetPtr == null) {
                break;
            }
            int flowId = packetPtr.flowId;
            int error;
            synchronized (common.G.lock) {
                error = decoderPtr.process(packetPtr, (packetPtr.length));
            }
            byte[] data;
            int[] decodedFlowId = new int[1];
            synchronized (common.G.lock) {
                data = decoderPtr.getComplete(decodedFlowId);
            }
            if (data != null) {
                int err = detectorPtr.process(data);
                if (err != 0) {
                    boolean status = errorVectorPtr.vector_pushBack(new Integer(decodedFlowId[0]));
                }
            }
        }
    }

    public void run() {
        Barrier.enterBarrier();
        processPackets(argument);
        Barrier.enterBarrier();
    }

    public static void main(String[] argv) {
        ERROR er = new ERROR();
        Intruder in = new Intruder(argv);
        Barrier.setBarrier(in.numThread);
        System.out.println("Percent attack  =   " + in.percentAttack);
        System.out.println("Max data length =   " + in.maxDataLength);
        System.out.println("Num flow        =   " + in.numFlow);
        System.out.println("Random seed     =   " + in.randomSeed);
        Dictionary dictionaryPtr = new Dictionary();
        Stream streamPtr = new Stream(in.percentAttack);
        int numAttack = streamPtr.generate(dictionaryPtr, in.numFlow, in.randomSeed, in.maxDataLength);
        System.out.println("Num Attack      =   " + numAttack);
        Decoder decoderPtr = new Decoder();
        Vector_t[] errorVectors = new Vector_t[in.numThread];
        int i;
        for (i = 0; i < in.numThread; i++) {
            errorVectors[i] = new Vector_t(in.numFlow);
        }
        Arg arg = new Arg();
        arg.streamPtr = streamPtr;
        arg.decoderPtr = decoderPtr;
        arg.errorVectors = errorVectors;
        in.argument = arg;
        Intruder[] intruders = new Intruder[in.numThread];
        for (i = 1; i < in.numThread; i++) {
            intruders[i] = new Intruder(i, arg);
        }
        in.threadID = 0;
        long start = System.currentTimeMillis();
        for (i = 1; i < in.numThread; i++) {
            intruders[i].start();
        }
       
        Barrier.enterBarrier();
        in.processPackets(in.argument);
        Barrier.enterBarrier();
        
        for (i = 1; i < in.numThread; i++) {
            try {
				intruders[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        System.out.println("TIME=" + elapsed);
        int numFound = 0;
        for (i = 0; i < in.numThread; i++) {
            Vector_t errorVectorPtr = errorVectors[i];
            int e;
            int numError = errorVectorPtr.vector_getSize();
            numFound += numError;
            for (e = 0; e < numError; e++) {
                int flowId = ((Integer) errorVectorPtr.vector_at(e)).intValue();
                boolean status = streamPtr.isAttack(flowId);
                if (status == false) {
                    System.out.println("Assertion in check solution");
                    System.exit(1);
                }
            }
        }
        System.out.println("Num found       = " + numFound);
        if (numFound != numAttack) {
            System.out.println("Assertion in check solution");
            System.exit(1);
        }
        System.out.println("Finished");
        System.exit(0);
    }
}
