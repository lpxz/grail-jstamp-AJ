package KMeans;

import Yada.java.Barrier;

public class Normal {

    int CHUNK;

    public Normal() {
        CHUNK = 3;
    }

    public static void work(int myId, GlobalArgs args) {
        int CHUNK = 3;
        float[][] feature = args.feature;
        int nfeatures = args.nfeatures;
        int npoints = args.npoints;
        int nclusters = args.nclusters;
        int[] membership = args.membership;
        float[][] clusters = args.clusters;
        int[] new_centers_len = args.new_centers_len;
        float[][] new_centers = args.new_centers;
        float delta = 0.0f;
        int index, start, stop;
        start = myId * CHUNK;
        while (start < npoints) {
            stop = (((start + CHUNK) < npoints) ? (start + CHUNK) : npoints);
            for (int i = start; i < stop; i++) {
                index = Common.common_findNearestPoint(feature[i], nfeatures, clusters, nclusters);
                if (membership[i] != index) {
                    delta += 1.0f;
                }
                membership[i] = index;
                synchronized (common.G.lock) {
                    new_centers_len[index] = new_centers_len[index] + 1;
                    for (int j = 0; j < nfeatures; j++) {
                        new_centers[index][j] = new_centers[index][j] + feature[i][j];
                    }
                }
            }
            if (start + CHUNK < npoints) {
                synchronized (common.G.lock) {
                    start = args.global_i;
                    args.global_i = start + CHUNK;
                }
            } else {
                break;
            }
        }
        synchronized (common.G.lock) {
            args.global_delta = args.global_delta + delta;
        }
    }

    public float[][] normal_exec(int nthreads, float[][] feature, int nfeatures, int npoints, int nclusters, float threshold, int[] membership, Random randomPtr, GlobalArgs args) {
        float delta;
        float[][] clusters;
        clusters = new float[nclusters][nfeatures];
        for (int i = 0; i < nclusters; i++) {
            int n = (int) (randomPtr.random_generate() % npoints);
            for (int j = 0; j < nfeatures; j++) {
                clusters[i][j] = feature[n][j];
            }
        }
        for (int i = 0; i < npoints; i++) {
            membership[i] = -1;
        }
        int[] new_centers_len = new int[nclusters];
        float[][] new_centers = new float[nclusters][nfeatures];
        int loop = 0;
        long start = System.currentTimeMillis();
        do {
            delta = 0.0f;
            args.feature = feature;
            args.nfeatures = nfeatures;
            args.npoints = npoints;
            args.nclusters = nclusters;
            args.membership = membership;
            args.clusters = clusters;
            args.new_centers_len = new_centers_len;
            args.new_centers = new_centers;
            args.global_i = nthreads * CHUNK;
            args.global_delta = delta;
            thread_work(args);
            delta = args.global_delta;
            for (int i = 0; i < nclusters; i++) {
                for (int j = 0; j < nfeatures; j++) {
                    if (new_centers_len[i] > 0) {
                        clusters[i][j] = new_centers[i][j] / new_centers_len[i];
                    }
                    new_centers[i][j] = (float) 0.0;
                }
                new_centers_len[i] = 0;
            }
            delta /= npoints;
        } while ((delta > threshold) && (loop++ < 500));
        long stop = System.currentTimeMillis();
        args.global_time += (stop - start);
        return clusters;
    }

    /**
   * Work done by primary thread in parallel with other threads
   **/
    void thread_work(GlobalArgs args) {
        Barrier.enterBarrier();
        Normal.work(0, args);
        Barrier.enterBarrier();
    }
}
