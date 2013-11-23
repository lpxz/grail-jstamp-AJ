package MatrixMultiply;

public class MatrixMultiply extends Thread {

    MMul mmul;

    public int x0, y0, x1, y1;

    public MatrixMultiply(MMul mmul, int x0, int x1, int y0, int y1) {
        this.mmul = mmul;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public void run() {
        synchronized (common.G.lock) {
            int M = mmul.M;
            int l = 8;
            for (int i = x0; i < x1; i++, l++) {
                for (int j = y0; j < y1; j++) {
                    double innerProduct = 0;
                    for (int k = 0; k < M; k++) {
                        innerProduct += mmul.a[i][k] * mmul.btranspose[j][k];
                    }
                    mmul.c[i][j] = innerProduct;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int NUM_THREADS = 4;
        int SIZE = 6000000;
        if (args.length > 0) {
            NUM_THREADS = Integer.parseInt(args[0]);
            if (args.length > 1) SIZE = Integer.parseInt(args[1]);
        }
        int p, q, r;
        MatrixMultiply[] mm;
        MatrixMultiply tmp;
        MMul matrix;
        matrix = new MMul(SIZE, SIZE, SIZE);
        matrix.setValues();
        matrix.transpose();
        mm = new MatrixMultiply[NUM_THREADS];
        int increment = SIZE / NUM_THREADS;
        int base = 0;
        for (int i = 0; i < NUM_THREADS; i++) {
            if ((i + 1) == NUM_THREADS) mm[i] = new MatrixMultiply(matrix, base, SIZE, 0, SIZE); else mm[i] = new MatrixMultiply(matrix, base, base + increment, 0, SIZE);
            base += increment;
        }
        p = matrix.L;
        q = matrix.M;
        r = matrix.N;
        System.out.println("\n");
        System.out.println("MatrixMultiply: L=");
        System.out.println(p);
        System.out.println("\t");
        System.out.println("M=");
        System.out.println(q);
        System.out.println("\t");
        System.out.println("N=");
        System.out.println(r);
        System.out.println("\n");
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            tmp = mm[i];
            tmp.start();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            tmp = mm[i];
            tmp.join();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time\n" + (end - start));
        System.out.println("Finished\n");
    }
}

class MMul {

    public int L, M, N;

    public double[][] a;

    public double[][] b;

    public double[][] c;

    public double[][] btranspose;

    public MMul(int L, int M, int N) {
        this.L = L;
        this.M = M;
        this.N = N;
        a = new double[L][M];
        b = new double[M][N];
        c = new double[L][N];
        btranspose = new double[N][M];
    }

    public void setValues() {
        for (int i = 0; i < L; i++) {
            double ai[] = a[i];
            for (int j = 0; j < M; j++) {
                ai[j] = j + 1;
            }
        }
        for (int i = 0; i < M; i++) {
            double bi[] = b[i];
            for (int j = 0; j < N; j++) {
                bi[j] = j + 1;
            }
        }
        for (int i = 0; i < L; i++) {
            double ci[] = c[i];
            for (int j = 0; j < N; j++) {
                ci[j] = 0;
            }
        }
        for (int i = 0; i < N; i++) {
            double btransposei[] = btranspose[i];
            for (int j = 0; j < M; j++) {
                btransposei[j] = 0;
            }
        }
    }

    public void transpose() {
        for (int row = 0; row < M; row++) {
            double brow[] = b[row];
            for (int col = 0; col < N; col++) {
                btranspose[col][row] = brow[col];
            }
        }
    }
}
