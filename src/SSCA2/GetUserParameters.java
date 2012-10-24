package SSCA2;

public class GetUserParameters {

    public GetUserParameters(Globals glb) {
        glb.THREADS = 1;
        glb.SCALE = 20;
        glb.MAX_PARAL_EDGES = 3;
        glb.PERC_INT_WEIGHTS = (float) 0.6f;
        glb.PROB_UNIDIRECTIONAL = (float) 0.1f;
        glb.PROB_INTERCL_EDGES = (float) 0.5f;
        glb.SUBGR_EDGE_LENGTH = 3;
        glb.K3_DS = 2;
    }

    public static void displayUsage() {
        System.out.println("Usage: ./SSCA.bin [options]");
        System.out.println("    i <float>    Probability [i]nter-clique      ");
        System.out.println("    k <int>   [k]ind: 0=array 1=list 2=vector ");
        System.out.println("    l <int>   Max path [l]ength               ");
        System.out.println("    p <int>   Max [p]arallel edges            ");
        System.out.println("    s <int>   Problem [s]cale                 ");
        System.out.println("    t <int>   Number of [t]hreads             ");
        System.out.println("    u <float>    Probability [u]nidirectional    ");
        System.out.println("    w <float>    Fraction integer [w]eights      ");
        System.exit(-1);
    }

    public void parseArgs(String[] args, Globals glb) {
        int i = 0;
        String arg;
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("-i")) {
                if (i < args.length) {
                    glb.PROB_INTERCL_EDGES = new Integer(args[i++]).floatValue();
                }
            } else if (arg.equals("-k")) {
                if (i < args.length) {
                    glb.K3_DS = new Integer(args[i++]).intValue();
                }
                if (!(glb.K3_DS >= 0 && glb.K3_DS <= 2)) {
                    System.out.println("Input a valid number for -k option between >=0 and <= 2");
                    System.exit(0);
                }
            } else if (arg.equals("-l")) {
                if (i < args.length) {
                    glb.SUBGR_EDGE_LENGTH = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-p")) {
                if (i < args.length) {
                    glb.MAX_PARAL_EDGES = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-s")) {
                if (i < args.length) {
                    glb.SCALE = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-t")) {
                if (i < args.length) {
                    glb.THREADS = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-u")) {
                if (i < args.length) {
                    glb.PROB_UNIDIRECTIONAL = new Integer(args[i++]).floatValue();
                }
            } else if (arg.equals("-w")) {
                if (i < args.length) {
                    glb.PERC_INT_WEIGHTS = new Integer(args[i++]).floatValue();
                }
            } else if (arg.equals("-h")) {
                displayUsage();
            }
        }
        if (glb.THREADS == 0) {
            System.out.println("Num processors cannot be Zero\n");
            displayUsage();
        }
        if ((glb.THREADS & (glb.THREADS - 1)) != 0) {
            System.out.println("Number of [t]hreads must be power of 2\n");
            displayUsage();
        }
    }

    public void getUserParameters(String[] argv, Globals glb) {
        glb.THREADS = 1;
        glb.SCALE = 20;
        glb.MAX_PARAL_EDGES = 3;
        glb.PERC_INT_WEIGHTS = (float) 0.6;
        glb.PROB_UNIDIRECTIONAL = (float) 0.1;
        glb.PROB_INTERCL_EDGES = (float) 0.5;
        glb.SUBGR_EDGE_LENGTH = 3;
        glb.K3_DS = 2;
        parseArgs(argv, glb);
        glb.TOT_VERTICES = (1 << glb.SCALE);
        glb.MAX_CLIQUE_SIZE = (1 << (glb.SCALE / 3));
        glb.MAX_INT_WEIGHT = (1 << glb.SCALE);
        glb.MAX_STRLEN = glb.SCALE;
        glb.SOUGHT_STRING = new byte[1];
        glb.MAX_CLUSTER_SIZE = glb.MAX_CLIQUE_SIZE;
    }
}
