package KMeans;

public class Common {

    public Common() {
    }

    public static float common_euclidDist2(float[] pt1, float[] pt2, int numdims) {
        int i;
        float ans = 0.0f;
        for (i = 0; i < numdims; i++) {
            ans += (pt1[i] - pt2[i]) * (pt1[i] - pt2[i]);
        }
        return ans;
    }

    public static int common_findNearestPoint(float[] pt, int nfeatures, float[][] pts, int npts) {
        int index = -1;
        int i;
        float max_dist = (float) 3.40282347e+38f;
        float limit = (float) 0.99999;
        for (i = 0; i < npts; i++) {
            float dist = common_euclidDist2(pt, pts[i], nfeatures);
            if ((dist / max_dist) < limit) {
                max_dist = dist;
                index = i;
                if (max_dist == 0) {
                    break;
                }
            }
        }
        return index;
    }
}
