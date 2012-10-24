package SSCA2;

import Yada.java.Barrier;

public class ComputeGraph {
   /*atomicset(C)*/
	
	/*atomicset(G)*/
    
	/*atomicset(Y)*/
	
	/*atomic(Y)*/public Graph GPtr /*I=this.Y*/;

    public GraphSDG SDGdataPtr;

    public int[] global_p;

   /*atomic(C)*/ public int global_maxNumVertices;

   /*atomic(G)*/ public int global_outVertexListSize;

    public int[][] global_impliedEdgeList;

    public int[][] global_auxArr;

    public ComputeGraph() {
        global_p = null;
        global_maxNumVertices = 0;
        global_outVertexListSize = 0;
        global_impliedEdgeList = null;
        global_auxArr = null;
    }

    public int NOSHARE(int x) {
        x = x << 7;
        return x;
    }

    public void prefix_sums(int myId, int numThread, int[] result, int[] input, int arraySize) {
        int[] p;
        if (myId == 0) {
            p = new int[NOSHARE(numThread)];
            global_p = p;
        }
        Barrier.enterBarrier();
        p = global_p;
        int start;
        int end;
        int r = arraySize / numThread;
        start = myId * r + 1;
        end = (myId + 1) * r;
        if (myId == (numThread - 1)) {
            end = arraySize;
        }
        for (int j = start; j < end; j++) {
            result[j] = input[j - 1] + result[j - 1];
        }
        p[NOSHARE(myId)] = result[end - 1];
        Barrier.enterBarrier();
        if (myId == 0) {
            for (int j = 1; j < numThread; j++) {
                p[NOSHARE(j)] += p[NOSHARE(j - 1)];
            }
        }
        Barrier.enterBarrier();
        if (myId > 0) {
            int add_value = p[NOSHARE(myId - 1)];
            for (int j = start - 1; j < end; j++) {
                result[j] += add_value;
            }
        }
        Barrier.enterBarrier();
    }

    public void prefix_sumsin(int myId, int numThread, int[] result, int[] input, int arraySize) {
        int[] p;
        if (myId == 0) {
            p = new int[NOSHARE(numThread)];
            global_p = p;
        }
        Barrier.enterBarrier();
        p = global_p;
        int start;
        int end;
        int r = arraySize / numThread;
        start = myId * r + 1;
        end = (myId + 1) * r;
        if (myId == (numThread - 1)) {
            end = arraySize;
        }
        for (int j = start; j < end; j++) {
            result[j] = input[j - 1] + result[j - 1];
        }
        p[NOSHARE(myId)] = result[end - 1];
        Barrier.enterBarrier();
        if (myId == 0) {
            for (int j = 1; j < numThread; j++) {
                p[NOSHARE(j)] += p[NOSHARE(j - 1)];
            }
        }
        Barrier.enterBarrier();
        if (myId > 0) {
            int add_value = p[NOSHARE(myId - 1)];
            for (int j = start - 1; j < end; j++) {
                result[j] += add_value;
            }
        }
        Barrier.enterBarrier();
    }

    public static void computeGraph(int myId, int numThread, Globals glb, ComputeGraph computeGraphArgs) {
        int maxNumVertices = 0;
        int numEdgesPlaced = computeGraphArgs.SDGdataPtr.numEdgesPlaced;
        Graph GPtr = computeGraphArgs.GPtr;
        GraphSDG SDGdataPtr = computeGraphArgs.SDGdataPtr;
        int MAX_CLUSTER_SIZE = glb.MAX_CLUSTER_SIZE;
        LocalStartStop lss = new LocalStartStop();
        CreatePartition.createPartition(0, numEdgesPlaced, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            if (SDGdataPtr.startVertex[i] > maxNumVertices) {
                maxNumVertices = SDGdataPtr.startVertex[i];
            }
        }
        synchronized (common.G.lock) {
            int tmp_maxNumVertices = computeGraphArgs.global_maxNumVertices;
            int new_maxNumVertices = ((CreatePartition.MAX(tmp_maxNumVertices, maxNumVertices)) + 1);
            computeGraphArgs.global_maxNumVertices = new_maxNumVertices;
        }
        Barrier.enterBarrier();
        maxNumVertices = computeGraphArgs.global_maxNumVertices;
        if (myId == 0) {
            {
                int realMaxNumVertices = (int) (Math.pow(2, glb.SCALE));
                if (maxNumVertices == realMaxNumVertices) ;
                maxNumVertices++;
            }
            GPtr.numVertices = maxNumVertices;
            GPtr.numEdges = numEdgesPlaced;
            GPtr.intWeight = SDGdataPtr.intWeight;
            GPtr.strWeight = SDGdataPtr.strWeight;
            for (int i = 0; i < numEdgesPlaced; i++) {
                if (GPtr.intWeight[numEdgesPlaced - i - 1] < 0) {
                    GPtr.numStrEdges = -(GPtr.intWeight[numEdgesPlaced - i - 1]) + 1;
                    GPtr.numIntEdges = numEdgesPlaced - GPtr.numStrEdges;
                    break;
                }
            }
            GPtr.outDegree = new int[GPtr.numVertices];
            GPtr.outVertexIndex = new int[GPtr.numVertices];
        }
        Barrier.enterBarrier();
        CreatePartition.createPartition(0, GPtr.numVertices, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            GPtr.outDegree[i] = 0;
            GPtr.outVertexIndex[i] = 0;
        }
        int outVertexListSize = 0;
        Barrier.enterBarrier();
        int i0 = -1;
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            int k = i;
            if ((outVertexListSize == 0) && (k != 0)) {
                while (i0 == -1) {
                    for (int j = 0; j < numEdgesPlaced; j++) {
                        if (k == SDGdataPtr.startVertex[j]) {
                            i0 = j;
                            break;
                        }
                    }
                    k--;
                }
            }
            if ((outVertexListSize == 0) && (k == 0)) {
                i0 = 0;
            }
            for (int j = i0; j < numEdgesPlaced; j++) {
                if (i == GPtr.numVertices - 1) {
                    break;
                }
                if ((i != SDGdataPtr.startVertex[j])) {
                    if ((j > 0) && (i == SDGdataPtr.startVertex[j - 1])) {
                        if (j - i0 >= 1) {
                            outVertexListSize++;
                            GPtr.outDegree[i]++;
                            for (int t = (i0 + 1); t < j; t++) {
                                if (SDGdataPtr.endVertex[t] != SDGdataPtr.endVertex[t - 1]) {
                                    outVertexListSize++;
                                    GPtr.outDegree[i] = GPtr.outDegree[i] + 1;
                                }
                            }
                        }
                    }
                    i0 = j;
                    break;
                }
            }
            if (i == GPtr.numVertices - 1) {
                if (numEdgesPlaced - i0 >= 0) {
                    outVertexListSize++;
                    GPtr.outDegree[i]++;
                    for (int t = (i0 + 1); t < numEdgesPlaced; t++) {
                        if (SDGdataPtr.endVertex[t] != SDGdataPtr.endVertex[t - 1]) {
                            outVertexListSize++;
                            GPtr.outDegree[i]++;
                        }
                    }
                }
            }
        }
        Barrier.enterBarrier();
        computeGraphArgs.prefix_sums(myId, numThread, GPtr.outVertexIndex, GPtr.outDegree, GPtr.numVertices);
        Barrier.enterBarrier();
        synchronized (common.G.lock) {
            computeGraphArgs.global_outVertexListSize = computeGraphArgs.global_outVertexListSize + outVertexListSize;
        }
        Barrier.enterBarrier();
        outVertexListSize = computeGraphArgs.global_outVertexListSize;
        if (myId == 0) {
            GPtr.numDirectedEdges = outVertexListSize;
            GPtr.outVertexList = new int[outVertexListSize];
            GPtr.paralEdgeIndex = new int[outVertexListSize];
            GPtr.outVertexList[0] = SDGdataPtr.endVertex[0];
        }
        Barrier.enterBarrier();
        i0 = -1;
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            int k = i;
            while ((i0 == -1) && (k != 0)) {
                for (int j = 0; j < numEdgesPlaced; j++) {
                    if (k == SDGdataPtr.startVertex[j]) {
                        i0 = j;
                        break;
                    }
                }
                k--;
            }
            if ((i0 == -1) && (k == 0)) {
                i0 = 0;
            }
            for (int j = i0; j < numEdgesPlaced; j++) {
                if (i == GPtr.numVertices - 1) {
                    break;
                }
                if (i != SDGdataPtr.startVertex[j]) {
                    if ((j > 0) && (i == SDGdataPtr.startVertex[j - 1])) {
                        if (j - i0 >= 1) {
                            int ii = (GPtr.outVertexIndex[i]);
                            int r = 0;
                            GPtr.paralEdgeIndex[ii] = i0;
                            GPtr.outVertexList[ii] = SDGdataPtr.endVertex[i0];
                            r++;
                            for (int t = (i0 + 1); t < j; t++) {
                                if (SDGdataPtr.endVertex[t] != SDGdataPtr.endVertex[t - 1]) {
                                    GPtr.paralEdgeIndex[ii + r] = t;
                                    GPtr.outVertexList[ii + r] = SDGdataPtr.endVertex[t];
                                    r++;
                                }
                            }
                        }
                    }
                    i0 = j;
                    break;
                }
            }
            if (i == GPtr.numVertices - 1) {
                int r = 0;
                if (numEdgesPlaced - i0 >= 0) {
                    int ii = GPtr.outVertexIndex[i];
                    GPtr.paralEdgeIndex[ii + r] = i0;
                    GPtr.outVertexList[ii + r] = SDGdataPtr.endVertex[i0];
                    r++;
                    for (int t = i0 + 1; t < numEdgesPlaced; t++) {
                        if (SDGdataPtr.endVertex[t] != SDGdataPtr.endVertex[t - 1]) {
                            GPtr.paralEdgeIndex[ii + r] = t;
                            GPtr.outVertexList[ii + r] = SDGdataPtr.endVertex[t];
                            r++;
                        }
                    }
                }
            }
        }
        Barrier.enterBarrier();
        if (myId == 0) {
            SDGdataPtr.startVertex = null;
            SDGdataPtr.endVertex = null;
            GPtr.inDegree = new int[GPtr.numVertices];
            GPtr.inVertexIndex = new int[GPtr.numVertices];
        }
        Barrier.enterBarrier();
        int[][] impliedEdgeList;
        if (myId == 0) {
            impliedEdgeList = new int[GPtr.numVertices][MAX_CLUSTER_SIZE];
            computeGraphArgs.global_impliedEdgeList = impliedEdgeList;
        }
        Barrier.enterBarrier();
        impliedEdgeList = computeGraphArgs.global_impliedEdgeList;
        CreatePartition.createPartition(0, GPtr.numVertices, myId, numThread, lss);
        CreatePartition.createPartition(0, (GPtr.numVertices * MAX_CLUSTER_SIZE), myId, numThread, lss);
        int[][] auxArr;
        if (myId == 0) {
            auxArr = new int[GPtr.numVertices][];
            computeGraphArgs.global_auxArr = auxArr;
        }
        Barrier.enterBarrier();
        auxArr = computeGraphArgs.global_auxArr;
        CreatePartition.createPartition(0, GPtr.numVertices, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            int jend = GPtr.outVertexIndex[i] + GPtr.outDegree[i];
            for (int j = GPtr.outVertexIndex[i]; j < jend; j++) {
                int v = (GPtr.outVertexList[j]);
                int k;
                int kend = GPtr.outVertexIndex[v] + GPtr.outDegree[v];
                for (k = GPtr.outVertexIndex[v]; k < kend; k++) {
                    if (GPtr.outVertexList[k] == i) {
                        break;
                    }
                }
                if (k == kend) {
                    synchronized (common.G.lock) {
                        int inDegree = GPtr.inDegree[v];
                        GPtr.inDegree[v] = (inDegree + 1);
                        if (inDegree < MAX_CLUSTER_SIZE) {
                            impliedEdgeList[v][inDegree] = i;
                        } else {
                            int a[];
                            if ((inDegree % MAX_CLUSTER_SIZE) == 0) {
                                a = new int[MAX_CLUSTER_SIZE];
                                auxArr[v] = a;
                            } else {
                                a = auxArr[v];
                            }
                            a[inDegree % MAX_CLUSTER_SIZE] = i;
                        }
                    }
                }
            }
        }
        Barrier.enterBarrier();
        computeGraphArgs.prefix_sumsin(myId, numThread, GPtr.inVertexIndex, GPtr.inDegree, GPtr.numVertices);
        if (myId == 0) {
            GPtr.numUndirectedEdges = GPtr.inVertexIndex[GPtr.numVertices - 1] + GPtr.inDegree[GPtr.numVertices - 1];
            GPtr.inVertexList = new int[GPtr.numUndirectedEdges];
        }
        Barrier.enterBarrier();
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            for (int j = GPtr.inVertexIndex[i]; j < (GPtr.inVertexIndex[i] + GPtr.inDegree[i]); j++) {
                if ((j - GPtr.inVertexIndex[i]) < MAX_CLUSTER_SIZE) {
                    GPtr.inVertexList[j] = impliedEdgeList[i][j - GPtr.inVertexIndex[i]];
                } else {
                    GPtr.inVertexList[j] = (auxArr[i])[(j - GPtr.inVertexIndex[i]) % MAX_CLUSTER_SIZE];
                }
            }
        }
        Barrier.enterBarrier();
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            if (GPtr.inDegree[i] > MAX_CLUSTER_SIZE) {
                auxArr[i] = null;
            }
        }
        Barrier.enterBarrier();
    }
}
