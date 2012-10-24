package SSCA2;

import Yada.java.Barrier;

public class Alg_Radix_Smp {

    public int[] global_myHisto;

    public int[] global_psHisto;

    public int[] global_lTemp;

    public int[] global_lTemp2;

    public Alg_Radix_Smp() {
        global_myHisto = null;
        global_psHisto = null;
        global_lTemp = null;
        global_lTemp2 = null;
    }

    public static int BITS(int x, int k, int j) {
        return ((x >> k) & ~(~0 << j));
    }

    public void all_countsort_node_aux_seq(int q, int[] lKey, int[] lSorted, int[] auxKey, int[] auxSorted, int R, int bitOff, int m) {
        int[] myHisto = new int[R];
        int[] psHisto = new int[R];
        for (int k = 0; k < R; k++) {
            myHisto[k] = 0;
        }
        for (int k = 0; k < q; k++) {
            myHisto[BITS(lKey[k], bitOff, m)]++;
        }
        int last;
        for (int k = 0; k < R; k++) {
            last = psHisto[k] = myHisto[k];
        }
        int offset = 0;
        for (int k = 0; k < R; k++) {
            myHisto[k] = (psHisto[k] - myHisto[k]) + offset;
            offset += psHisto[k];
        }
        for (int k = 0; k < q; k++) {
            int j = BITS(lKey[k], bitOff, m);
            lSorted[myHisto[j]] = lKey[k];
            auxSorted[myHisto[j]] = auxKey[k];
            myHisto[j]++;
        }
    }

    public void all_countsort_node_aux(int myId, int numThread, int q, int[] lKey, int[] lSorted, int[] auxKey, int[] auxSorted, int R, int bitOff, int m, Alg_Radix_Smp rdxsort) {
        int[] myHisto = null;
        int[] psHisto = null;
        if (myId == 0) {
            myHisto = new int[numThread * R];
            rdxsort.global_myHisto = myHisto;
            psHisto = new int[numThread * R];
            rdxsort.global_psHisto = psHisto;
        }
        Barrier.enterBarrier();
        myHisto = rdxsort.global_myHisto;
        psHisto = rdxsort.global_psHisto;
        for (int k = 0; k < R; k++) {
            myHisto[((myId * R) + k)] = 0;
        }
        LocalStartStop lss = new LocalStartStop();
        CreatePartition.createPartition(0, q, myId, numThread, lss);
        for (int k = lss.i_start; k < lss.i_stop; k++) {
            myHisto[(myId * R) + BITS(lKey[k], bitOff, m)]++;
        }
        Barrier.enterBarrier();
        CreatePartition.createPartition(0, R, myId, numThread, lss);
        int last;
        for (int k = lss.i_start; k < lss.i_stop; k++) {
            last = psHisto[k] = myHisto[k];
            for (int j = 1; j < numThread; j++) {
                int temp = psHisto[(j * R + k)] = last + myHisto[(j * R + k)];
                last = temp;
            }
        }
        Barrier.enterBarrier();
        int offset = 0;
        for (int k = 0; k < R; k++) {
            myHisto[(myId * R) + k] = (psHisto[(myId * R) + k] - myHisto[(myId * R) + k]) + offset;
            offset += psHisto[((numThread - 1) * R) + k];
        }
        Barrier.enterBarrier();
        CreatePartition.createPartition(0, q, myId, numThread, lss);
        for (int k = lss.i_start; k < lss.i_stop; k++) {
            int j = BITS(lKey[k], bitOff, m);
            lSorted[myHisto[(myId * R) + j]] = lKey[k];
            auxSorted[myHisto[(myId * R) + j]] = auxKey[k];
            myHisto[(myId * R) + j]++;
        }
        Barrier.enterBarrier();
        if (myId == 0) {
            psHisto = null;
            myHisto = null;
        }
    }

    public void all_radixsort_node_aux_s3_seq(int q, int[] lKeys, int[] lSorted, int[] auxKey, int[] auxSorted) {
        int[] lTemp = new int[q];
        int[] lTemp2 = new int[q];
        all_countsort_node_aux_seq(q, lKeys, lSorted, auxKey, auxSorted, (1 << 11), 0, 11);
        all_countsort_node_aux_seq(q, lSorted, lTemp, auxSorted, lTemp2, (1 << 11), 11, 11);
        all_countsort_node_aux_seq(q, lTemp, lSorted, lTemp2, auxSorted, (1 << 10), 22, 10);
    }

    public static void all_radixsort_node_aux_s3(int myId, int numThread, int q, int[] lKeys, int[] lSorted, int[] auxKey, int[] auxSorted, Alg_Radix_Smp rdxsort) {
        int[] lTemp = null;
        int[] lTemp2 = null;
        if (myId == 0) {
            lTemp = new int[q];
            rdxsort.global_lTemp = lTemp;
            lTemp2 = new int[q];
            rdxsort.global_lTemp2 = lTemp2;
        }
        Barrier.enterBarrier();
        lTemp = rdxsort.global_lTemp;
        lTemp2 = rdxsort.global_lTemp2;
        rdxsort.all_countsort_node_aux(myId, numThread, q, lKeys, lSorted, auxKey, auxSorted, (1 << 11), 0, 11, rdxsort);
        rdxsort.all_countsort_node_aux(myId, numThread, q, lSorted, lTemp, auxSorted, lTemp2, (1 << 11), 11, 11, rdxsort);
        rdxsort.all_countsort_node_aux(myId, numThread, q, lTemp, lSorted, lTemp2, auxSorted, (1 << 10), 22, 10, rdxsort);
        Barrier.enterBarrier();
    }
}
