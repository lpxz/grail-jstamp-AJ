package Bayes;

public class Adtree {

    int numVar;

    int numRecord;

    AdtreeNode rootNodePtr;

    public Adtree() {
        numVar = -1;
        numRecord = -1;
        rootNodePtr = null;
    }

    public AdtreeVary makeVary(int parentIndex, int index, int start, int numRecord, Data dataPtr) {
        AdtreeVary varyPtr = new AdtreeVary(index);
        if ((parentIndex + 1 != index) && (numRecord > 1)) {
            dataPtr.data_sort(start, numRecord, index);
        }
        int num0 = dataPtr.data_findSplit(start, numRecord, index);
        int num1 = numRecord - num0;
        int mostCommonValue = ((num0 >= num1) ? 0 : 1);
        varyPtr.mostCommonValue = mostCommonValue;
        if (num0 == 0 || mostCommonValue == 0) {
        } else {
            varyPtr.zeroNodePtr = makeNode(index, index, start, num0, dataPtr);
            varyPtr.zeroNodePtr.value = 0;
        }
        if (num1 == 0 || mostCommonValue == 1) {
        } else {
            varyPtr.oneNodePtr = makeNode(index, index, (start + num0), num1, dataPtr);
            varyPtr.oneNodePtr.value = 1;
        }
        return varyPtr;
    }

    public AdtreeNode makeNode(int parentIndex, int index, int start, int numRecord, Data dataPtr) {
        int numVar = dataPtr.numVar;
        AdtreeNode nodePtr = new AdtreeNode(index, numVar - index - 1);
        nodePtr.count = numRecord;
        AdtreeVary varyVectorPtr[] = nodePtr.varyVectorPtr;
        int i = 0;
        for (int v = (index + 1); v < numVar; v++) {
            AdtreeVary varyPtr = makeVary(parentIndex, v, start, numRecord, dataPtr);
            varyVectorPtr[i++] = varyPtr;
        }
        return nodePtr;
    }

    public void adtree_make(Data dataPtr) {
        numVar = dataPtr.numVar;
        numRecord = dataPtr.numRecord;
        dataPtr.data_sort(0, numRecord, 0);
        rootNodePtr = makeNode(-1, -1, 0, numRecord, dataPtr);
    }

    public int getCount(AdtreeNode nodePtr, int i, int q, Vector_t queryVectorPtr, int lastQueryIndex) {
        if (nodePtr == null) {
            return 0;
        }
        int nodeIndex = nodePtr.index;
        if (nodeIndex >= lastQueryIndex) {
            return nodePtr.count;
        }
        int count = 0;
        Query queryPtr = (Query) (queryVectorPtr.vector_at(q));
        if (queryPtr == null) {
            return nodePtr.count;
        }
        int queryIndex = queryPtr.index;
        AdtreeVary varyPtr = nodePtr.varyVectorPtr[queryIndex - nodeIndex - 1];
        int queryValue = queryPtr.value;
        if (queryValue == varyPtr.mostCommonValue) {
            int numQuery = queryVectorPtr.vector_getSize();
            Vector_t superQueryVectorPtr = new Vector_t(numQuery - 1);
            for (int qq = 0; qq < numQuery; qq++) {
                if (qq != q) {
                    boolean status = superQueryVectorPtr.vector_pushBack(queryVectorPtr.vector_at(qq));
                }
            }
            int superCount = adtree_getCount(superQueryVectorPtr);
            superQueryVectorPtr.clear();
            int invertCount;
            if (queryValue == 0) {
                queryPtr.value = 1;
                invertCount = getCount(nodePtr, i, q, queryVectorPtr, lastQueryIndex);
                queryPtr.value = 0;
            } else {
                queryPtr.value = 0;
                invertCount = getCount(nodePtr, i, q, queryVectorPtr, lastQueryIndex);
                queryPtr.value = 1;
            }
            count += superCount - invertCount;
        } else {
            if (queryValue == 0) {
                count += getCount(varyPtr.zeroNodePtr, (i + 1), (q + 1), queryVectorPtr, lastQueryIndex);
            } else if (queryValue == 1) {
                count += getCount(varyPtr.oneNodePtr, (i + 1), (q + 1), queryVectorPtr, lastQueryIndex);
            }
        }
        return count;
    }

    public int adtree_getCount(Vector_t queryVectorPtr) {
        if (rootNodePtr == null) {
            return 0;
        }
        int lastQueryIndex = -1;
        int numQuery = queryVectorPtr.vector_getSize();
        if (numQuery > 0) {
            Query lastQueryPtr = (Query) (queryVectorPtr.vector_at(numQuery - 1));
            lastQueryIndex = lastQueryPtr.index;
        }
        return getCount(rootNodePtr, -1, 0, queryVectorPtr, lastQueryIndex);
    }
}
