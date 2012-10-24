package Genome;

import java.util.LinkedList;

public class Table {

    LinkedList buckets[];

    int numBucket;

    Table(int myNumBucket) {
        buckets = new LinkedList[myNumBucket];
        for (int i = 0; i < myNumBucket; i++) {
            buckets[i] = new LinkedList();
        }
        numBucket = myNumBucket;
    }

    boolean table_insert(int hash, Object dataPtr) {
        int i = hash % numBucket;
        if (i < 0) i = -i;
        if (buckets[i].contains(dataPtr)) {
            return false;
        }
        buckets[i].add(dataPtr);
        return true;
    }

    boolean table_remove(int hash, Object dataPtr) {
        int i = (hash % numBucket);
        if (i < 0) i = -i;
        boolean tempbool = buckets[i].contains(dataPtr);
        if (tempbool) {
            buckets[i].remove(dataPtr);
            return true;
        }
        return false;
    }
}
