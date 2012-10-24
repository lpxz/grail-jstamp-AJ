package SSCA2;

public class CreatePartition {

    public CreatePartition() {
    }

    public static void createPartition(int min, int max, int id, int n, LocalStartStop lss) {
        int range = max - min;
        int chunk = MAX(1, ((range + n / 2) / n));
        int start = min + chunk * id;
        int stop;
        if (id == (n - 1)) {
            stop = max;
        } else {
            stop = MIN(max, (start + chunk));
        }
        lss.i_start = start;
        lss.i_stop = stop;
    }

    public static int MAX(int a, int b) {
        int val = (a > b) ? (a) : (b);
        return val;
    }

    public static int MIN(int a, int b) {
        int val = (a < b) ? (a) : (b);
        return val;
    }
}
