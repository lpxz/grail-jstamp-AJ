package KMeans;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import Yada.java.Barrier;

public class KMeans extends Thread {

    int max_nclusters;

    int min_nclusters;

    int isBinaryFile;

    int use_zscore_transform;

    String filename;

    int nthreads;

    float threshold;

    int threadid;

    GlobalArgs g_args;

    int best_nclusters;

    float[][] cluster_centres;

    public KMeans() {
        max_nclusters = 13;
        min_nclusters = 4;
        isBinaryFile = 0;
        use_zscore_transform = 1;
        threshold = (float) 0.001;
        best_nclusters = 0;
    }

    public KMeans(int threadid, GlobalArgs g_args) {
        this.threadid = threadid;
        this.g_args = g_args;
    }

    public void run() {
        {
            Barrier.enterBarrier();
            Normal.work(threadid, g_args);
            Barrier.enterBarrier();
        }
    }

    public static void main(String[] args) {
        int nthreads;
        int MAX_LINE_LENGTH = 1000000;
        KMeans kms = new KMeans();
        KMeans.parseCmdLine(args, kms);
        nthreads = kms.nthreads;
        if (kms.max_nclusters < kms.min_nclusters) {
            System.out.println("Error: max_clusters must be >= min_clusters\n");
            System.exit(0);
        }
        float[][] buf;
        float[][] attributes;
        int numAttributes = 0;
        int numObjects = 0;
        if (kms.isBinaryFile == 1) {
            System.out.println("TODO: Unimplemented Binary file option\n");
            System.exit(0);
        }
        FileInputStream inputFile = null;
        try {
            inputFile = new FileInputStream(kms.filename);
            byte b[] = new byte[MAX_LINE_LENGTH];
            int n;
            while ((n = inputFile.read(b)) != -1) {
                for (int i = 0; i < n; i++) {
                    if (b[i] == '\n') numObjects++;
                }
            }
            inputFile.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(kms.filename));
            String line = null;
            if ((line = br.readLine()) != null) {
                int index = 0;
                boolean prevWhiteSpace = true;
                while (index < line.length()) {
                    char c = line.charAt(index++);
                    boolean currWhiteSpace = Character.isWhitespace(c);
                    if (prevWhiteSpace && !currWhiteSpace) {
                        numAttributes++;
                    }
                    prevWhiteSpace = currWhiteSpace;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        numAttributes = numAttributes - 1;
        System.out.println("numObjects= " + numObjects + " numAttributes= " + numAttributes);
        buf = new float[numObjects][numAttributes];
        attributes = new float[numObjects][numAttributes];
        try {
            KMeans.readFromFile(inputFile, kms.filename, buf, MAX_LINE_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished Reading from file ......");
        int nloops = 1;
        int len = kms.max_nclusters - kms.min_nclusters + 1;
        KMeans[] km = new KMeans[nthreads];
        GlobalArgs g_args = new GlobalArgs();
        g_args.nthreads = nthreads;
        System.out.println("num of threads: " + nthreads);
        for (int i = 1; i < nthreads; i++) {
            km[i] = new KMeans(i, g_args);
        }
        long start = System.currentTimeMillis();
        for (int i = 1; i < nthreads; i++) {
            km[i].start();
        }
        System.out.println("Finished Starting threads......");
        for (int i = 0; i < nloops; i++) {
            for (int x = 0; x < numObjects; x++) {
                for (int y = 0; y < numAttributes; y++) {
                    attributes[x][y] = buf[x][y];
                }
            }
            Cluster.cluster_exec(nthreads, numObjects, numAttributes, attributes, kms, g_args);
        }
        for (int i = 1; i < nthreads; i++) {
            try {
                km[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("TIME=" + (end - start));
        System.out.println("Printing output......");
        System.out.println("Best_nclusters= " + kms.best_nclusters);
        {
            for (int i = 0; i < kms.best_nclusters; i++) {
                System.out.print(i + " ");
                for (int j = 0; j < numAttributes; j++) {
                    System.out.print(kms.cluster_centres[i][j] + " ");
                }
                System.out.println("\n");
            }
        }
        System.out.println("Finished......");
        System.exit(0);
    }

    public static void parseCmdLine(String args[], KMeans km) {
        int i = 0;
        String arg;
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("-m")) {
                if (i < args.length) {
                    km.max_nclusters = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-n")) {
                if (i < args.length) {
                    km.min_nclusters = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-t")) {
                if (i < args.length) {
                    km.threshold = (float) Double.parseDouble(args[i++]);
                }
            } else if (arg.equals("-i")) {
                if (i < args.length) {
                    km.filename = args[i++];
                }
            } else if (arg.equals("-b")) {
                if (i < args.length) {
                    km.isBinaryFile = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-z")) {
                km.use_zscore_transform = 0;
            } else if (arg.equals("-nthreads")) {
                if (i < args.length) {
                    km.nthreads = new Integer(args[i++]).intValue();
                }
            } else if (arg.equals("-h")) {
                km.usage();
            }
        }
        if (km.nthreads == 0 || km.filename == null) {
            km.usage();
        }
    }

    public void usage() {
        System.out.println("usage: ./kmeans -m <max_clusters> -n <min_clusters> -t <threshold> -i <filename> -nthreads <threads>\n");
        System.out.println("  -i filename:     file containing data to be clustered\n");
        System.out.println("  -b               input file is in binary format\n");
        System.out.println("  -m max_clusters: maximum number of clusters allowed\n");
        System.out.println("  -n min_clusters: minimum number of clusters allowed\n");
        System.out.println("  -z             : don't zscore transform data\n");
        System.out.println("  -t threshold   : threshold value\n");
        System.out.println("  -nthreads      : number of threads\n");
    }

    public static void readFromFile(FileInputStream inputFile, String filename, float[][] buf, int MAX_LINE_LENGTH) throws Exception {
        inputFile = new FileInputStream(filename);
        int j;
        int i = 0;
        byte b[] = new byte[MAX_LINE_LENGTH];
        int n;
        byte oldbytes[] = null;
        j = -1;
        while ((n = inputFile.read(b)) != -1) {
            int x = 0;
            if (oldbytes != null) {
                boolean cr = false;
                for (; x < n; x++) {
                    if (b[x] == ' ') break;
                    if (b[x] == '\n') {
                        cr = true;
                        break;
                    }
                }
                byte newbytes[] = new byte[x + oldbytes.length];
                boolean isnumber = false;
                for (int ii = 0; ii < oldbytes.length; ii++) {
                    if (oldbytes[ii] >= '0' && oldbytes[ii] <= '9') isnumber = true;
                    newbytes[ii] = oldbytes[ii];
                }
                for (int ii = 0; ii < x; ii++) {
                    if (b[ii] >= '0' && b[ii] <= '9') isnumber = true;
                    newbytes[ii + oldbytes.length] = b[ii];
                }
                if (x != n) x++;
                if (isnumber) {
                    if (j >= 0) {
                        buf[i][j] = (float) Double.parseDouble(new String(newbytes, 0, newbytes.length));
                    }
                    j++;
                }
                if (cr) {
                    j = -1;
                    i++;
                }
                oldbytes = null;
            }
            while (x < n) {
                int y = x;
                boolean cr = false;
                boolean isnumber = false;
                for (y = x; y < n; y++) {
                    if ((b[y] >= '0') && (b[y] <= '9')) isnumber = true;
                    if (b[y] == ' ') break;
                    if (b[y] == '\n') {
                        cr = true;
                        break;
                    }
                }
                if (y == n) {
                    oldbytes = new byte[y - x];
                    for (int ii = 0; ii < (y - x); ii++) oldbytes[ii] = b[ii + x];
                    break;
                }
                if (isnumber) {
                    if (j >= 0) {
                        buf[i][j] = (float) Double.parseDouble(new String(b, x, y - x));
                    }
                    j++;
                }
                if (cr) {
                    i++;
                    j = -1;
                    x = y;
                    x++;
                } else {
                    x = y;
                    x++;
                }
            }
        }
        inputFile.close();
    }
}
