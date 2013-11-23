package KMeans;

public class GlobalArgs {

    public GlobalArgs() {
    }

    public int nthreads;

    public float[][] feature;

    public int nfeatures;

    public int npoints;

    public int nclusters;

    public int[] membership;

    public float[][] clusters;

    public int[] new_centers_len;

    public float[][] new_centers;

    public int global_i;

    public float global_delta;

    long global_time;
}
