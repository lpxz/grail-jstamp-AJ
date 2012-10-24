package KMeans;

public class Cluster {

    public Cluster() {
    }

    public static float[] extractMoments(float[] data, int num_elts, int num_moments) {
        float[] moments = new float[num_moments];
        for (int i = 0; i < num_elts; i++) {
            moments[0] += data[i];
        }
        moments[0] = moments[0] / num_elts;
        for (int j = 1; j < num_moments; j++) {
            moments[j] = 0;
            for (int i = 0; i < num_elts; i++) {
                moments[j] = (float) (moments[j] + Math.pow((data[i] - moments[0]), j + 1));
            }
            moments[j] = moments[j] / num_elts;
        }
        return moments;
    }

    public static void zscoreTransform(float[][] data, int numObjects, int numAttributes) {
        float[] moments;
        float[] single_variable = new float[numObjects];
        for (int i = 0; i < numAttributes; i++) {
            for (int j = 0; j < numObjects; j++) {
                single_variable[j] = data[j][i];
            }
            moments = extractMoments(single_variable, numObjects, 2);
            moments[1] = (float) Math.sqrt((double) moments[1]);
            for (int j = 0; j < numObjects; j++) {
                data[j][i] = (data[j][i] - moments[0]) / moments[1];
            }
        }
    }

    public static void cluster_exec(int nthreads, int numObjects, int numAttributes, float[][] attributes, KMeans kms, GlobalArgs args) {
        int itime;
        int nclusters;
        float[][] tmp_cluster_centres;
        int[] membership = new int[numObjects];
        Random randomPtr = new Random();
        randomPtr.random_alloc();
        if (kms.use_zscore_transform == 1) {
            zscoreTransform(attributes, numObjects, numAttributes);
        }
        itime = 0;
        for (nclusters = kms.min_nclusters; nclusters <= kms.max_nclusters; nclusters++) {
            randomPtr.random_seed(7);
            Normal norm = new Normal();
            tmp_cluster_centres = norm.normal_exec(nthreads, attributes, numAttributes, numObjects, nclusters, kms.threshold, membership, randomPtr, args);
            kms.cluster_centres = tmp_cluster_centres;
            kms.best_nclusters = nclusters;
            itime++;
        }
    }
}
