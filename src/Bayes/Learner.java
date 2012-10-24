
package Bayes;

import KMeans.Common;
import common.BitMap;
import common.LocalStartStop;

public class Learner {

    private static final boolean TEST_LEARNER = false;

    private static final boolean LEARNER_TRY_REMOVE = false;

    private static final boolean LEARNER_TRY_REVERSE = false;

    private static final int CACHE_LINE_SIZE = 64;

    private static final int QUERY_VALUE_WILDCARD = -1;

    private static final int OPERATION_INSERT = 0;

    private static final int OPERATION_REMOVE = 1;

    private static final int OPERATION_REVERSE = 2;

    private static final int NUM_OPERATION = 3;

    Adtree adtreePtr;

    Net netPtr;

    float[] localBaseLogLikelihoods;

    float baseLogLikelihood;

    LearnerTask[] tasks;

    List taskListPtr;

    int numTotalParent;

    int global_insertPenalty;

    int global_maxNumEdgeLearned;

    float global_operationQualityFactor;

    public Learner() {
        if (TEST_LEARNER) {
            global_maxNumEdgeLearned = -1;
            global_insertPenalty = 1;
            global_operationQualityFactor = 1.0F;
        }
    }

    public Learner(Data dataPtr, Adtree adtreePtr, int numThread, int global_insertPenalty, int global_maxNumEdgeLearned, float global_operationQualityFactor) {
        this.adtreePtr = adtreePtr;
        this.netPtr = new Net(dataPtr.numVar);
        this.localBaseLogLikelihoods = new float[dataPtr.numVar];
        this.baseLogLikelihood = 0.0f;
        this.tasks = new LearnerTask[dataPtr.numVar];
        this.taskListPtr = List.list_alloc();
        this.numTotalParent = 0;
        if (TEST_LEARNER) {
            this.global_insertPenalty = global_insertPenalty;
            this.global_maxNumEdgeLearned = global_maxNumEdgeLearned;
            this.global_operationQualityFactor = global_operationQualityFactor;
        }
    }

    public void learner_free() {
        adtreePtr = null;
        netPtr = null;
        localBaseLogLikelihoods = null;
        tasks = null;
        taskListPtr = null;
    }

    public float computeSpecificLocalLogLikelihood(Adtree adtreePtr, Vector_t queryVectorPtr, Vector_t parentQueryVectorPtr) {
        int count = adtreePtr.adtree_getCount(queryVectorPtr);
        if (count == 0) {
            return 0.0f;
        }
        double probability = (double) count / (double) adtreePtr.numRecord;
        int parentCount = adtreePtr.adtree_getCount(parentQueryVectorPtr);
        float fval = (float) (probability * (Math.log((double) count / (double) parentCount)));
        return fval;
    }

    public void createPartition(int min, int max, int id, int n, LocalStartStop lss) {
        int range = max - min;
        int chunk = Math.max(1, ((range + n / 2) / n));
        int start = min + chunk * id;
        int stop;
        if (id == (n - 1)) {
            stop = max;
        } else {
            stop = Math.min(max, (start + chunk));
        }
        lss.i_start = start;
        lss.i_stop = stop;
    }

    public static void createTaskList(int myId, int numThread, Learner learnerPtr) {
        boolean status;
        Query[] queries = new Query[2];
        queries[0] = new Query();
        queries[1] = new Query();
        Vector_t queryVectorPtr = new Vector_t(2);
        status = queryVectorPtr.vector_pushBack(queries[0]);
        Query parentQuery = new Query();
        Vector_t parentQueryVectorPtr = new Vector_t(1);
        int numVar = learnerPtr.adtreePtr.numVar;
        int numRecord = learnerPtr.adtreePtr.numRecord;
        float baseLogLikelihood = 0.0f;
        float penalty = (float) (-0.5f * Math.log((double) numRecord));
        LocalStartStop lss = new LocalStartStop();
        learnerPtr.createPartition(0, numVar, myId, numThread, lss);
        for (int v = lss.i_start; v < lss.i_stop; v++) {
            float localBaseLogLikelihood = 0.0f;
            queries[0].index = v;
            queries[0].value = 0;
            localBaseLogLikelihood += learnerPtr.computeSpecificLocalLogLikelihood(learnerPtr.adtreePtr, queryVectorPtr, parentQueryVectorPtr);
            queries[0].value = 1;
            localBaseLogLikelihood += learnerPtr.computeSpecificLocalLogLikelihood(learnerPtr.adtreePtr, queryVectorPtr, parentQueryVectorPtr);
            learnerPtr.localBaseLogLikelihoods[v] = localBaseLogLikelihood;
            baseLogLikelihood += localBaseLogLikelihood;
        }
        synchronized (common.G.lock) {
            {
                float globalBaseLogLikelihood = learnerPtr.baseLogLikelihood;
                learnerPtr.baseLogLikelihood = (baseLogLikelihood + globalBaseLogLikelihood);
            }
        }
        status = parentQueryVectorPtr.vector_pushBack(parentQuery);
        for (int v = lss.i_start; v < lss.i_stop; v++) {
            queries[0].index = v;
            int bestLocalIndex = v;
            float bestLocalLogLikelihood = learnerPtr.localBaseLogLikelihoods[v];
            status = queryVectorPtr.vector_pushBack(queries[1]);
            for (int vv = 0; vv < numVar; vv++) {
                if (vv == v) {
                    continue;
                }
                parentQuery.index = vv;
                if (v < vv) {
                    queries[0].index = v;
                    queries[1].index = vv;
                } else {
                    queries[0].index = vv;
                    queries[1].index = v;
                }
                float newLocalLogLikelihood = 0.0f;
                queries[0].value = 0;
                queries[1].value = 0;
                parentQuery.value = 0;
                newLocalLogLikelihood += learnerPtr.computeSpecificLocalLogLikelihood(learnerPtr.adtreePtr, queryVectorPtr, parentQueryVectorPtr);
                queries[0].value = 0;
                queries[1].value = 1;
                parentQuery.value = ((vv < v) ? 0 : 1);
                newLocalLogLikelihood += learnerPtr.computeSpecificLocalLogLikelihood(learnerPtr.adtreePtr, queryVectorPtr, parentQueryVectorPtr);
                queries[0].value = 1;
                queries[1].value = 0;
                parentQuery.value = ((vv < v) ? 1 : 0);
                newLocalLogLikelihood += learnerPtr.computeSpecificLocalLogLikelihood(learnerPtr.adtreePtr, queryVectorPtr, parentQueryVectorPtr);
                queries[0].value = 1;
                queries[1].value = 1;
                parentQuery.value = 1;
                newLocalLogLikelihood += learnerPtr.computeSpecificLocalLogLikelihood(learnerPtr.adtreePtr, queryVectorPtr, parentQueryVectorPtr);
                if (newLocalLogLikelihood > bestLocalLogLikelihood) {
                    bestLocalIndex = vv;
                    bestLocalLogLikelihood = newLocalLogLikelihood;
                }
            }
            queryVectorPtr.vector_popBack();
            if (bestLocalIndex != v) {
                float logLikelihood = numRecord * (baseLogLikelihood + +bestLocalLogLikelihood - learnerPtr.localBaseLogLikelihoods[v]);
                float score = penalty + logLikelihood;
                learnerPtr.tasks[v] = new LearnerTask();
                LearnerTask taskPtr = learnerPtr.tasks[v];
                taskPtr.op = OPERATION_INSERT;
                taskPtr.fromId = bestLocalIndex;
                taskPtr.toId = v;
                taskPtr.score = score;
                status = learnerPtr.taskListPtr.list_insert(taskPtr);
            }
        }
        queryVectorPtr.clear();
        parentQueryVectorPtr.clear();
        if (TEST_LEARNER) {
            ListNode it = learnerPtr.taskListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                LearnerTask taskPtr = it.dataPtr;
                System.out.println("[task] op= " + taskPtr.op + " from= " + taskPtr.fromId + " to= " + taskPtr.toId + " score= " + taskPtr.score);
            }
        }
    }

    public LearnerTask TMpopTask(List taskListPtr) {
        LearnerTask taskPtr = null;
        ListNode it = taskListPtr.head;
        if (it.nextPtr != null) {
            it = it.nextPtr;
            taskPtr = it.dataPtr;
            boolean status = taskListPtr.list_remove(taskPtr);
        }
        return taskPtr;
    }

    public void populateParentQueryVector(Net netPtr, int id, Query[] queries, Vector_t parentQueryVectorPtr) {
        parentQueryVectorPtr.vector_clear();
        IntList parentIdListPtr = netPtr.net_getParentIdListPtr(id);
        IntListNode it = parentIdListPtr.head;
        while (it.nextPtr != null) {
            it = it.nextPtr;
            int parentId = it.dataPtr;
            boolean status = parentQueryVectorPtr.vector_pushBack(queries[parentId]);
        }
    }

    public void TMpopulateParentQueryVector(Net netPtr, int id, Query[] queries, Vector_t parentQueryVectorPtr) {
        parentQueryVectorPtr.vector_clear();
        IntList parentIdListPtr = netPtr.net_getParentIdListPtr(id);
        IntListNode it = parentIdListPtr.head;
        while (it.nextPtr != null) {
            it = it.nextPtr;
            int parentId = it.dataPtr;
            boolean status = parentQueryVectorPtr.vector_pushBack(queries[parentId]);
        }
    }

    public void populateQueryVectors(Net netPtr, int id, Query[] queries, Vector_t queryVectorPtr, Vector_t parentQueryVectorPtr) {
        populateParentQueryVector(netPtr, id, queries, parentQueryVectorPtr);
        boolean status;
        status = Vector_t.vector_copy(queryVectorPtr, parentQueryVectorPtr);
        status = queryVectorPtr.vector_pushBack(queries[id]);
        queryVectorPtr.vector_sort();
    }

    public void TMpopulateQueryVectors(Net netPtr, int id, Query[] queries, Vector_t queryVectorPtr, Vector_t parentQueryVectorPtr) {
        TMpopulateParentQueryVector(netPtr, id, queries, parentQueryVectorPtr);
        boolean status;
        status = Vector_t.vector_copy(queryVectorPtr, parentQueryVectorPtr);
        status = queryVectorPtr.vector_pushBack(queries[id]);
        queryVectorPtr.vector_sort();
    }

    public float computeLocalLogLikelihoodHelper(int i, int numParent, Adtree adtreePtr, Query[] queries, Vector_t queryVectorPtr, Vector_t parentQueryVectorPtr) {
        if (i >= numParent) {
            return computeSpecificLocalLogLikelihood(adtreePtr, queryVectorPtr, parentQueryVectorPtr);
        }
        float localLogLikelihood = 0.0f;
        Query parentQueryPtr = (Query) (parentQueryVectorPtr.vector_at(i));
        int parentIndex = parentQueryPtr.index;
        queries[parentIndex].value = 0;
        localLogLikelihood += computeLocalLogLikelihoodHelper((i + 1), numParent, adtreePtr, queries, queryVectorPtr, parentQueryVectorPtr);
        queries[parentIndex].value = 1;
        localLogLikelihood += computeLocalLogLikelihoodHelper((i + 1), numParent, adtreePtr, queries, queryVectorPtr, parentQueryVectorPtr);
        queries[parentIndex].value = QUERY_VALUE_WILDCARD;
        return localLogLikelihood;
    }

    public float computeLocalLogLikelihood(int id, Adtree adtreePtr, Net netPtr, Query[] queries, Vector_t queryVectorPtr, Vector_t parentQueryVectorPtr) {
        int numParent = parentQueryVectorPtr.vector_getSize();
        float localLogLikelihood = 0.0f;
        queries[id].value = 0;
        localLogLikelihood += computeLocalLogLikelihoodHelper(0, numParent, adtreePtr, queries, queryVectorPtr, parentQueryVectorPtr);
        queries[id].value = 1;
        localLogLikelihood += computeLocalLogLikelihoodHelper(0, numParent, adtreePtr, queries, queryVectorPtr, parentQueryVectorPtr);
        queries[id].value = QUERY_VALUE_WILDCARD;
        return localLogLikelihood;
    }

    public LearnerTask TMfindBestInsertTask(FindBestTaskArg argPtr) {
        int toId = argPtr.toId;
        Learner learnerPtr = argPtr.learnerPtr;
        Query[] queries = argPtr.queries;
        Vector_t queryVectorPtr = argPtr.queryVectorPtr;
        Vector_t parentQueryVectorPtr = argPtr.parentQueryVectorPtr;
        int numTotalParent = argPtr.numTotalParent;
        float basePenalty = argPtr.basePenalty;
        float baseLogLikelihood = argPtr.baseLogLikelihood;
        BitMap invalidBitmapPtr = argPtr.bitmapPtr;
        Queue workQueuePtr = argPtr.workQueuePtr;
        Vector_t baseParentQueryVectorPtr = argPtr.aQueryVectorPtr;
        Vector_t baseQueryVectorPtr = argPtr.bQueryVectorPtr;
        boolean status;
        Adtree adtreePtr = learnerPtr.adtreePtr;
        Net netPtr = learnerPtr.netPtr;
        TMpopulateParentQueryVector(netPtr, toId, queries, parentQueryVectorPtr);
        status = Vector_t.vector_copy(baseParentQueryVectorPtr, parentQueryVectorPtr);
        status = Vector_t.vector_copy(baseQueryVectorPtr, baseParentQueryVectorPtr);
        status = baseQueryVectorPtr.vector_pushBack(queries[toId]);
        queryVectorPtr.vector_sort();
        int bestFromId = toId;
        float oldLocalLogLikelihood = learnerPtr.localBaseLogLikelihoods[toId];
        float bestLocalLogLikelihood = oldLocalLogLikelihood;
        status = netPtr.net_findDescendants(toId, invalidBitmapPtr, workQueuePtr);
        int fromId = -1;
        IntList parentIdListPtr = netPtr.net_getParentIdListPtr(toId);
        int maxNumEdgeLearned = global_maxNumEdgeLearned;
        if ((maxNumEdgeLearned < 0) || (parentIdListPtr.list_getSize() <= maxNumEdgeLearned)) {
            IntListNode it = parentIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int parentId = it.dataPtr;
                invalidBitmapPtr.bitmap_set(parentId);
            }
            while ((fromId = invalidBitmapPtr.bitmap_findClear((fromId + 1))) >= 0) {
                if (fromId == toId) {
                    continue;
                }
                status = Vector_t.vector_copy(queryVectorPtr, baseQueryVectorPtr);
                status = queryVectorPtr.vector_pushBack(queries[fromId]);
                queryVectorPtr.vector_sort();
                status = Vector_t.vector_copy(parentQueryVectorPtr, baseParentQueryVectorPtr);
                status = parentQueryVectorPtr.vector_pushBack(queries[fromId]);
                parentQueryVectorPtr.vector_sort();
                float newLocalLogLikelihood = computeLocalLogLikelihood(toId, adtreePtr, netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
                if (newLocalLogLikelihood > bestLocalLogLikelihood) {
                    bestLocalLogLikelihood = newLocalLogLikelihood;
                    bestFromId = fromId;
                }
            }
        }
        LearnerTask bestTask = new LearnerTask();
        bestTask.op = OPERATION_INSERT;
        bestTask.fromId = bestFromId;
        bestTask.toId = toId;
        bestTask.score = 0.0f;
        if (bestFromId != toId) {
            int numRecord = adtreePtr.numRecord;
            int numParent = parentIdListPtr.list_getSize() + 1;
            float penalty = (numTotalParent + numParent * global_insertPenalty) * basePenalty;
            float logLikelihood = numRecord * (baseLogLikelihood + +bestLocalLogLikelihood - oldLocalLogLikelihood);
            float bestScore = penalty + logLikelihood;
            bestTask.score = bestScore;
        }
        return bestTask;
    }

    LearnerTask TMfindBestRemoveTask(FindBestTaskArg argPtr) {
        int toId = argPtr.toId;
        Learner learnerPtr = argPtr.learnerPtr;
        Query[] queries = argPtr.queries;
        Vector_t queryVectorPtr = argPtr.queryVectorPtr;
        Vector_t parentQueryVectorPtr = argPtr.parentQueryVectorPtr;
        int numTotalParent = argPtr.numTotalParent;
        float basePenalty = argPtr.basePenalty;
        float baseLogLikelihood = argPtr.baseLogLikelihood;
        Vector_t origParentQueryVectorPtr = argPtr.aQueryVectorPtr;
        boolean status;
        Adtree adtreePtr = learnerPtr.adtreePtr;
        Net netPtr = learnerPtr.netPtr;
        float[] localBaseLogLikelihoods = learnerPtr.localBaseLogLikelihoods;
        TMpopulateParentQueryVector(netPtr, toId, queries, origParentQueryVectorPtr);
        int numParent = origParentQueryVectorPtr.vector_getSize();
        int bestFromId = toId;
        float oldLocalLogLikelihood = localBaseLogLikelihoods[toId];
        float bestLocalLogLikelihood = oldLocalLogLikelihood;
        int i;
        for (i = 0; i < numParent; i++) {
            Query queryPtr = (Query) (origParentQueryVectorPtr.vector_at(i));
            int fromId = queryPtr.index;
            parentQueryVectorPtr.vector_clear();
            for (int p = 0; p < numParent; p++) {
                if (p != fromId) {
                    Query tmpqueryPtr = (Query) (origParentQueryVectorPtr.vector_at(p));
                    status = parentQueryVectorPtr.vector_pushBack(queries[tmpqueryPtr.index]);
                }
            }
            status = Vector_t.vector_copy(queryVectorPtr, parentQueryVectorPtr);
            status = queryVectorPtr.vector_pushBack(queries[toId]);
            queryVectorPtr.vector_sort();
            float newLocalLogLikelihood = computeLocalLogLikelihood(toId, adtreePtr, netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
            if (newLocalLogLikelihood > bestLocalLogLikelihood) {
                bestLocalLogLikelihood = newLocalLogLikelihood;
                bestFromId = fromId;
            }
        }
        LearnerTask bestTask = new LearnerTask();
        bestTask.op = OPERATION_REMOVE;
        bestTask.fromId = bestFromId;
        bestTask.toId = toId;
        bestTask.score = 0.0f;
        if (bestFromId != toId) {
            int numRecord = adtreePtr.numRecord;
            float penalty = (numTotalParent - 1) * basePenalty;
            float logLikelihood = numRecord * (baseLogLikelihood + +bestLocalLogLikelihood - oldLocalLogLikelihood);
            float bestScore = penalty + logLikelihood;
            bestTask.score = bestScore;
        }
        return bestTask;
    }

    public LearnerTask TMfindBestReverseTask(FindBestTaskArg argPtr) {
        int toId = argPtr.toId;
        Learner learnerPtr = argPtr.learnerPtr;
        Query[] queries = argPtr.queries;
        Vector_t queryVectorPtr = argPtr.queryVectorPtr;
        Vector_t parentQueryVectorPtr = argPtr.parentQueryVectorPtr;
        int numTotalParent = argPtr.numTotalParent;
        float basePenalty = argPtr.basePenalty;
        float baseLogLikelihood = argPtr.baseLogLikelihood;
        BitMap visitedBitmapPtr = argPtr.bitmapPtr;
        Queue workQueuePtr = argPtr.workQueuePtr;
        Vector_t toOrigParentQueryVectorPtr = argPtr.aQueryVectorPtr;
        Vector_t fromOrigParentQueryVectorPtr = argPtr.bQueryVectorPtr;
        boolean status;
        Adtree adtreePtr = learnerPtr.adtreePtr;
        Net netPtr = learnerPtr.netPtr;
        float[] localBaseLogLikelihoods = learnerPtr.localBaseLogLikelihoods;
        TMpopulateParentQueryVector(netPtr, toId, queries, toOrigParentQueryVectorPtr);
        int numParent = toOrigParentQueryVectorPtr.vector_getSize();
        int bestFromId = toId;
        float oldLocalLogLikelihood = localBaseLogLikelihoods[toId];
        float bestLocalLogLikelihood = oldLocalLogLikelihood;
        int fromId = 0;
        for (int i = 0; i < numParent; i++) {
            Query queryPtr = (Query) (toOrigParentQueryVectorPtr.vector_at(i));
            fromId = queryPtr.index;
            bestLocalLogLikelihood = oldLocalLogLikelihood + localBaseLogLikelihoods[fromId];
            TMpopulateParentQueryVector(netPtr, fromId, queries, fromOrigParentQueryVectorPtr);
            parentQueryVectorPtr.vector_clear();
            for (int p = 0; p < numParent; p++) {
                if (p != fromId) {
                    Query tmpqueryPtr = (Query) (toOrigParentQueryVectorPtr.vector_at(p));
                    status = parentQueryVectorPtr.vector_pushBack(queries[tmpqueryPtr.index]);
                }
            }
            status = Vector_t.vector_copy(queryVectorPtr, parentQueryVectorPtr);
            status = queryVectorPtr.vector_pushBack(queries[toId]);
            queryVectorPtr.vector_sort();
            float newLocalLogLikelihood = computeLocalLogLikelihood(toId, adtreePtr, netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
            status = Vector_t.vector_copy(parentQueryVectorPtr, fromOrigParentQueryVectorPtr);
            status = parentQueryVectorPtr.vector_pushBack(queries[toId]);
            parentQueryVectorPtr.vector_sort();
            status = Vector_t.vector_copy(queryVectorPtr, parentQueryVectorPtr);
            status = queryVectorPtr.vector_pushBack(queries[fromId]);
            queryVectorPtr.vector_sort();
            newLocalLogLikelihood += computeLocalLogLikelihood(fromId, adtreePtr, netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
            if (newLocalLogLikelihood > bestLocalLogLikelihood) {
                bestLocalLogLikelihood = newLocalLogLikelihood;
                bestFromId = fromId;
            }
        }
        if (bestFromId != toId) {
            boolean isTaskValid = true;
            netPtr.net_applyOperation(OPERATION_REMOVE, bestFromId, toId);
            if (netPtr.net_isPath(bestFromId, toId, visitedBitmapPtr, workQueuePtr)) {
                isTaskValid = false;
            }
            netPtr.net_applyOperation(OPERATION_INSERT, bestFromId, toId);
            if (!isTaskValid) {
                bestFromId = toId;
            }
        }
        LearnerTask bestTask = new LearnerTask();
        bestTask.op = OPERATION_REVERSE;
        bestTask.fromId = bestFromId;
        bestTask.toId = toId;
        bestTask.score = 0.0f;
        if (bestFromId != toId) {
            float fromLocalLogLikelihood = localBaseLogLikelihoods[bestFromId];
            int numRecord = adtreePtr.numRecord;
            float penalty = numTotalParent * basePenalty;
            float logLikelihood = numRecord * (baseLogLikelihood + +bestLocalLogLikelihood - oldLocalLogLikelihood - fromLocalLogLikelihood);
            float bestScore = penalty + logLikelihood;
            bestTask.score = bestScore;
        }
        return bestTask;
    }

    public static void learnStructure(int myId, int numThread, Learner learnerPtr) {
        int numRecord = learnerPtr.adtreePtr.numRecord;
        float operationQualityFactor = learnerPtr.global_operationQualityFactor;
        BitMap visitedBitmapPtr = BitMap.bitmap_alloc(learnerPtr.adtreePtr.numVar);
        Queue workQueuePtr = Queue.queue_alloc(-1);
        int numVar = learnerPtr.adtreePtr.numVar;
        Query[] queries = new Query[numVar];
        for (int v = 0; v < numVar; v++) {
            queries[v] = new Query();
            queries[v].index = v;
            queries[v].value = QUERY_VALUE_WILDCARD;
        }
        float basePenalty = (float) (-0.5 * Math.log((double) numRecord));
        Vector_t queryVectorPtr = new Vector_t(1);
        Vector_t parentQueryVectorPtr = new Vector_t(1);
        Vector_t aQueryVectorPtr = new Vector_t(1);
        Vector_t bQueryVectorPtr = new Vector_t(1);
        FindBestTaskArg arg = new FindBestTaskArg();
        arg.learnerPtr = learnerPtr;
        arg.queries = queries;
        arg.queryVectorPtr = queryVectorPtr;
        arg.parentQueryVectorPtr = parentQueryVectorPtr;
        arg.bitmapPtr = visitedBitmapPtr;
        arg.workQueuePtr = workQueuePtr;
        arg.aQueryVectorPtr = aQueryVectorPtr;
        arg.bQueryVectorPtr = bQueryVectorPtr;
        while (true) {
            LearnerTask taskPtr;
            synchronized (common.G.lock) {
                taskPtr = learnerPtr.TMpopTask(learnerPtr.taskListPtr);
            }
            if (taskPtr == null) {
                break;
            }
            int op = taskPtr.op;
            int fromId = taskPtr.fromId;
            int toId = taskPtr.toId;
            boolean isTaskValid;
            synchronized (common.G.lock) {
                isTaskValid = true;
                if (op == OPERATION_INSERT) {
                    if (learnerPtr.netPtr.net_hasEdge(fromId, toId) || learnerPtr.netPtr.net_isPath(toId, fromId, visitedBitmapPtr, workQueuePtr)) {
                        isTaskValid = false;
                    }
                } else if (op == OPERATION_REMOVE) {
                    ;
                } else if (op == OPERATION_REVERSE) {
                    learnerPtr.netPtr.net_applyOperation(OPERATION_REMOVE, fromId, toId);
                    if (learnerPtr.netPtr.net_isPath(fromId, toId, visitedBitmapPtr, workQueuePtr)) {
                        isTaskValid = false;
                    }
                    learnerPtr.netPtr.net_applyOperation(OPERATION_INSERT, fromId, toId);
                }
                if (TEST_LEARNER) {
                    System.out.println("[task] op= " + taskPtr.op + " from= " + taskPtr.fromId + " to= " + taskPtr.toId + " score= " + taskPtr.score + " valid= " + (isTaskValid ? "yes" : "no"));
                }
                if (isTaskValid) {
                    learnerPtr.netPtr.net_applyOperation(op, fromId, toId);
                }
            }
            float deltaLogLikelihood = 0.0f;
            if (isTaskValid) {
                float newBaseLogLikelihood;
                if (op == OPERATION_INSERT) {
                    synchronized (common.G.lock) {
                        learnerPtr.TMpopulateQueryVectors(learnerPtr.netPtr, toId, queries, queryVectorPtr, parentQueryVectorPtr);
                        newBaseLogLikelihood = learnerPtr.computeLocalLogLikelihood(toId, learnerPtr.adtreePtr, learnerPtr.netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
                        float toLocalBaseLogLikelihood = learnerPtr.localBaseLogLikelihoods[toId];
                        deltaLogLikelihood += toLocalBaseLogLikelihood - newBaseLogLikelihood;
                        learnerPtr.localBaseLogLikelihoods[toId] = newBaseLogLikelihood;
                    }
                    synchronized (common.G.lock) {
                        int numTotalParent = learnerPtr.numTotalParent;
                        learnerPtr.numTotalParent = numTotalParent + 1;
                    }
                } else if (op == OPERATION_REMOVE) {
                    if (LEARNER_TRY_REMOVE) {
                        synchronized (common.G.lock) {
                            learnerPtr.TMpopulateQueryVectors(learnerPtr.netPtr, fromId, queries, queryVectorPtr, parentQueryVectorPtr);
                            newBaseLogLikelihood = learnerPtr.computeLocalLogLikelihood(fromId, learnerPtr.adtreePtr, learnerPtr.netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
                            float fromLocalBaseLogLikelihood = learnerPtr.localBaseLogLikelihoods[fromId];
                            deltaLogLikelihood += fromLocalBaseLogLikelihood - newBaseLogLikelihood;
                            learnerPtr.localBaseLogLikelihoods[fromId] = newBaseLogLikelihood;
                        }
                        synchronized (common.G.lock) {
                            int numTotalParent = learnerPtr.numTotalParent;
                            learnerPtr.numTotalParent = numTotalParent - 1;
                        }
                    }
                } else if (op == OPERATION_REVERSE) {
                    if (LEARNER_TRY_REVERSE) {
                        synchronized (common.G.lock) {
                            learnerPtr.TMpopulateQueryVectors(learnerPtr.netPtr, fromId, queries, queryVectorPtr, parentQueryVectorPtr);
                            newBaseLogLikelihood = learnerPtr.computeLocalLogLikelihood(fromId, learnerPtr.adtreePtr, learnerPtr.netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
                            float fromLocalBaseLogLikelihood = learnerPtr.localBaseLogLikelihoods[fromId];
                            deltaLogLikelihood += fromLocalBaseLogLikelihood - newBaseLogLikelihood;
                            learnerPtr.localBaseLogLikelihoods[fromId] = newBaseLogLikelihood;
                        }
                        synchronized (common.G.lock) {
                            learnerPtr.TMpopulateQueryVectors(learnerPtr.netPtr, toId, queries, queryVectorPtr, parentQueryVectorPtr);
                            newBaseLogLikelihood = learnerPtr.computeLocalLogLikelihood(toId, learnerPtr.adtreePtr, learnerPtr.netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
                            float toLocalBaseLogLikelihood = learnerPtr.localBaseLogLikelihoods[toId];
                            deltaLogLikelihood += toLocalBaseLogLikelihood - newBaseLogLikelihood;
                            learnerPtr.localBaseLogLikelihoods[toId] = newBaseLogLikelihood;
                        }
                    }
                }
            }
            float baseLogLikelihood;
            int numTotalParent;
            synchronized (common.G.lock) {
                float oldBaseLogLikelihood = learnerPtr.baseLogLikelihood;
                float newBaseLogLikelihood = oldBaseLogLikelihood + deltaLogLikelihood;
                learnerPtr.baseLogLikelihood = newBaseLogLikelihood;
                baseLogLikelihood = newBaseLogLikelihood;
                numTotalParent = learnerPtr.numTotalParent;
            }
            float baseScore = ((float) numTotalParent * basePenalty) + (numRecord * baseLogLikelihood);
            LearnerTask bestTask = new LearnerTask();
            bestTask.op = NUM_OPERATION;
            bestTask.toId = -1;
            bestTask.fromId = -1;
            bestTask.score = baseScore;
            LearnerTask newTask = new LearnerTask();
            arg.toId = toId;
            arg.numTotalParent = numTotalParent;
            arg.basePenalty = basePenalty;
            arg.baseLogLikelihood = baseLogLikelihood;
            synchronized (common.G.lock) {
                newTask = learnerPtr.TMfindBestInsertTask(arg);
            }
            if ((newTask.fromId != newTask.toId) && (newTask.score > (bestTask.score / operationQualityFactor))) {
                bestTask = newTask;
            }
            if (LEARNER_TRY_REMOVE) {
                synchronized (common.G.lock) {
                    newTask = learnerPtr.TMfindBestRemoveTask(arg);
                }
                if ((newTask.fromId != newTask.toId) && (newTask.score > (bestTask.score / operationQualityFactor))) {
                    bestTask = newTask;
                }
            }
            if (LEARNER_TRY_REVERSE) {
                synchronized (common.G.lock) {
                    newTask = learnerPtr.TMfindBestReverseTask(arg);
                }
                if ((newTask.fromId != newTask.toId) && (newTask.score > (bestTask.score / operationQualityFactor))) {
                    bestTask = newTask;
                }
            }
            if (bestTask.toId != -1) {
                LearnerTask[] tasks = learnerPtr.tasks;
                tasks[toId] = bestTask;
                synchronized (common.G.lock) {
                    learnerPtr.taskListPtr.list_insert(tasks[toId]);
                }
                if (TEST_LEARNER) {
                    System.out.println("[new]  op= " + bestTask.op + " from= " + bestTask.fromId + " to= " + bestTask.toId + " score= " + bestTask.score);
                }
            }
        }
        visitedBitmapPtr.bitmap_free();
        workQueuePtr.queue_free();
        bQueryVectorPtr.clear();
        aQueryVectorPtr.clear();
        queryVectorPtr.clear();
        parentQueryVectorPtr.clear();
        queries = null;
    }

    public void learner_run(int myId, int numThread, Learner learnerPtr) {
        {
            createTaskList(myId, numThread, learnerPtr);
        }
        {
            learnStructure(myId, numThread, learnerPtr);
        }
    }

    public float learner_score() {
        Vector_t queryVectorPtr = new Vector_t(1);
        Vector_t parentQueryVectorPtr = new Vector_t(1);
        int numVar = adtreePtr.numVar;
        Query[] queries = new Query[numVar];
        for (int v = 0; v < numVar; v++) {
            queries[v] = new Query();
            queries[v].index = v;
            queries[v].value = QUERY_VALUE_WILDCARD;
        }
        int numTotalParent = 0;
        float logLikelihood = 0.0f;
        for (int v = 0; v < numVar; v++) {
            IntList parentIdListPtr = netPtr.net_getParentIdListPtr(v);
            numTotalParent += parentIdListPtr.list_getSize();
            populateQueryVectors(netPtr, v, queries, queryVectorPtr, parentQueryVectorPtr);
            float localLogLikelihood = computeLocalLogLikelihood(v, adtreePtr, netPtr, queries, queryVectorPtr, parentQueryVectorPtr);
            logLikelihood += localLogLikelihood;
        }
        queryVectorPtr.clear();
        parentQueryVectorPtr.clear();
        queries = null;
        int numRecord = adtreePtr.numRecord;
        float penalty = (float) (-0.5f * (double) numTotalParent * Math.log((double) numRecord));
        float score = penalty + (float) numRecord * logLikelihood;
        return score;
    }
}
