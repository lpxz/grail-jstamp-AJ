package Bayes;

import common.BitMap;

public class Data {

    private static final byte DATA_INIT = 2;

    private static final int DATA_PRECISION = 100;

    int numVar;

    int numRecord;

    byte[] records;

    Random randomPtr;

    Sort sort;

    public Data(int numVar, int numRecord, Random randomPtr) {
        int numDatum = numVar * numRecord;
        records = new byte[numDatum];
        for (int i = 0; i < numDatum; i++) this.records[i] = (byte) DATA_INIT;
        this.numVar = numVar;
        this.numRecord = numRecord;
        this.randomPtr = randomPtr;
        this.sort = new Sort();
    }

    public void data_free() {
        records = null;
        randomPtr = null;
    }

    public Net data_generate(int seed, int maxNumParent, int percentParent) {
        if (seed >= 0) {
            randomPtr.random_seed(seed);
        }
        Net netPtr = new Net(numVar);
        netPtr.net_generateRandomEdges(maxNumParent, percentParent, randomPtr);
        int[][] thresholdsTable = new int[numVar][];
        int v;
        for (v = 0; v < numVar; v++) {
            IntList parentIdListPtr = netPtr.net_getParentIdListPtr(v);
            int numThreshold = 1 << parentIdListPtr.list_getSize();
            int[] thresholds = new int[numThreshold];
            for (int t = 0; t < numThreshold; t++) {
                int threshold = (int) (randomPtr.random_generate() % (DATA_PRECISION + 1));
                thresholds[t] = threshold;
            }
            thresholdsTable[v] = thresholds;
        }
        int[] order = new int[numVar];
        int numOrder = 0;
        Queue workQueuePtr = Queue.queue_alloc(-1);
        IntVector dependencyVectorPtr = IntVector.vector_alloc(1);
        BitMap orderedBitmapPtr = BitMap.bitmap_alloc(numVar);
        orderedBitmapPtr.bitmap_clearAll();
        BitMap doneBitmapPtr = BitMap.bitmap_alloc(numVar);
        doneBitmapPtr.bitmap_clearAll();
        v = -1;
        while ((v = doneBitmapPtr.bitmap_findClear(v + 1)) >= 0) {
            IntList childIdListPtr = netPtr.net_getChildIdListPtr(v);
            int numChild = childIdListPtr.list_getSize();
            if (numChild == 0) {
                boolean status;
                workQueuePtr.queue_clear();
                status = workQueuePtr.queue_push(v);
                while (!(workQueuePtr.queue_isEmpty())) {
                    int id = workQueuePtr.queue_pop();
                    status = doneBitmapPtr.bitmap_set(id);
                    status = dependencyVectorPtr.vector_pushBack(id);
                    IntList parentIdListPtr = netPtr.net_getParentIdListPtr(id);
                    IntListNode it = parentIdListPtr.head;
                    while (it.nextPtr != null) {
                        it = it.nextPtr;
                        int parentId = it.dataPtr;
                        status = workQueuePtr.queue_push(parentId);
                    }
                }
                int n = dependencyVectorPtr.vector_getSize();
                for (int i = 0; i < n; i++) {
                    int id = dependencyVectorPtr.vector_popBack();
                    if (!(orderedBitmapPtr.bitmap_isSet(id))) {
                        orderedBitmapPtr.bitmap_set(id);
                        order[numOrder++] = id;
                    }
                }
            }
        }
        int startindex = 0;
        for (int r = 0; r < numRecord; r++) {
            for (int o = 0; o < numOrder; o++) {
                v = order[o];
                IntList parentIdListPtr = netPtr.net_getParentIdListPtr(v);
                int index = 0;
                IntListNode it = parentIdListPtr.head;
                while (it.nextPtr != null) {
                    it = it.nextPtr;
                    int parentId = it.dataPtr;
                    int value = records[startindex + parentId];
                    index = (index << 1) + value;
                }
                int rnd = (int) (randomPtr.random_generate() % DATA_PRECISION);
                int threshold = thresholdsTable[v][index];
                records[startindex + v] = (byte) ((rnd < threshold) ? 1 : 0);
            }
            startindex += numVar;
        }
        return netPtr;
    }

    public static boolean data_copy(Data dstPtr, Data srcPtr) {
        int numDstDatum = dstPtr.numVar * dstPtr.numRecord;
        int numSrcDatum = srcPtr.numVar * srcPtr.numRecord;
        if (numDstDatum != numSrcDatum) {
            dstPtr.records = new byte[numSrcDatum];
        }
        dstPtr.numVar = srcPtr.numVar;
        dstPtr.numRecord = srcPtr.numRecord;
        for (int i = 0; i < numSrcDatum; i++) dstPtr.records[i] = srcPtr.records[i];
        return true;
    }

    public void data_sort(int start, int num, int offset) {
        sort.sort(records, start * numVar, num, numVar, numVar, offset);
    }

    public int data_findSplit(int start, int num, int offset) {
        int low = start;
        int high = start + num - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (records[numVar * mid + offset] == 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return (low - start);
    }
}
