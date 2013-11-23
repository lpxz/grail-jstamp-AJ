package SSCA2;

import common.Random;
import Yada.java.Barrier;

public class GenScalData {

    public int[] global_permV;

    public int[] global_cliqueSizes;

    public int global_totCliques;

    public int[] global_firstVsInCliques;

    public int[] global_lastVsInCliques;

    public int[] global_i_edgeStartCounter;

    public int[] global_i_edgeEndCounter;

    public int global_edgeNum;

    public int global_numStrWtEdges;

    public int[] global_startVertex;

    public int[] global_endVertex;

    public int[] global_tempIndex;

    public GenScalData() {
        global_permV = null;
        global_cliqueSizes = null;
        global_totCliques = 0;
        global_firstVsInCliques = null;
        global_lastVsInCliques = null;
        global_i_edgeStartCounter = null;
        global_i_edgeEndCounter = null;
        global_edgeNum = 0;
        global_numStrWtEdges = 0;
        global_startVertex = null;
        global_endVertex = null;
        global_tempIndex = null;
    }

    public static void genScalData_seq(Globals glb, GraphSDG SDGdataPtr, GenScalData gsd, Alg_Radix_Smp radixsort) {
        Random randomPtr = new Random();
        randomPtr.random_alloc();
        randomPtr.random_seed(0);
        int[] permV;
        permV = new int[glb.TOT_VERTICES];
        for (int i = 0; i < glb.TOT_VERTICES; i++) {
            permV[i] = i;
        }
        for (int i = 0; i < glb.TOT_VERTICES; i++) {
            int t1 = (int) (randomPtr.posrandom_generate());
            int t = i + t1 % (glb.TOT_VERTICES - i);
            if (t != i) {
                int t2 = permV[t];
                permV[t] = permV[i];
                permV[i] = t2;
            }
        }
        int[] cliqueSizes;
        int estTotCliques = (int) (Math.ceil(1.5 * glb.TOT_VERTICES / ((1 + glb.MAX_CLIQUE_SIZE) / 2)));
        cliqueSizes = new int[estTotCliques];
        for (int i = 0; i < estTotCliques; i++) {
            cliqueSizes[i] = (int) (1 + (randomPtr.posrandom_generate() % glb.MAX_CLIQUE_SIZE));
        }
        int totCliques = 0;
        int[] lastVsInCliques;
        int[] firstVsInCliques;
        lastVsInCliques = new int[estTotCliques];
        firstVsInCliques = new int[estTotCliques];
        lastVsInCliques[0] = cliqueSizes[0] - 1;
        {
            int i;
            for (i = 1; i < estTotCliques; i++) {
                lastVsInCliques[i] = cliqueSizes[i] + lastVsInCliques[i - 1];
                if (lastVsInCliques[i] >= glb.TOT_VERTICES - 1) {
                    break;
                }
            }
            totCliques = i + 1;
        }
        cliqueSizes[totCliques - 1] = glb.TOT_VERTICES - lastVsInCliques[totCliques - 2] - 1;
        lastVsInCliques[totCliques - 1] = glb.TOT_VERTICES - 1;
        firstVsInCliques[0] = 0;
        for (int i = 1; i < totCliques; i++) {
            firstVsInCliques[i] = lastVsInCliques[i - 1] + 1;
        }
        int estTotEdges;
        if (glb.SCALE >= 12) {
            estTotEdges = (int) (Math.ceil(((glb.MAX_CLIQUE_SIZE - 1) * glb.TOT_VERTICES)));
        } else {
            estTotEdges = (int) (Math.ceil(1.2 * (((glb.MAX_CLIQUE_SIZE - 1) * glb.TOT_VERTICES) * ((1 + glb.MAX_PARAL_EDGES) / 2) + glb.TOT_VERTICES * 2)));
        }
        int i_edgePtr = 0;
        float p = glb.PROB_UNIDIRECTIONAL;
        int numByte = estTotEdges;
        int[] startV = new int[numByte];
        int[] endV = new int[numByte];
        int[][] tmpEdgeCounter = new int[glb.MAX_CLIQUE_SIZE][glb.MAX_CLIQUE_SIZE];
        for (int i_clique = 0; i_clique < totCliques; i_clique++) {
            int i_cliqueSize = cliqueSizes[i_clique];
            int i_firstVsInClique = firstVsInCliques[i_clique];
            for (int i = 0; i < i_cliqueSize; i++) {
                for (int j = 0; j < i; j++) {
                    float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                    if (r >= p) {
                        startV[i_edgePtr] = i + i_firstVsInClique;
                        endV[i_edgePtr] = j + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[i][j] = 1;
                        startV[i_edgePtr] = j + i_firstVsInClique;
                        endV[i_edgePtr] = i + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[j][i] = 1;
                    } else if (r >= 0.5) {
                        startV[i_edgePtr] = i + i_firstVsInClique;
                        endV[i_edgePtr] = j + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[i][j] = 1;
                        tmpEdgeCounter[j][i] = 0;
                    } else {
                        startV[i_edgePtr] = j + i_firstVsInClique;
                        endV[i_edgePtr] = i + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[j][i] = 1;
                        tmpEdgeCounter[i][j] = 0;
                    }
                }
            }
            if (i_cliqueSize != 1) {
                int randNumEdges = (int) (randomPtr.posrandom_generate() % (2 * i_cliqueSize * glb.MAX_PARAL_EDGES));
                for (int i_paralEdge = 0; i_paralEdge < randNumEdges; i_paralEdge++) {
                    int i = (int) (randomPtr.posrandom_generate() % i_cliqueSize);
                    int j = (int) (randomPtr.posrandom_generate() % i_cliqueSize);
                    if ((i != j) && (tmpEdgeCounter[i][j] < glb.MAX_PARAL_EDGES)) {
                        float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                        if (r >= p) {
                            startV[i_edgePtr] = i + i_firstVsInClique;
                            endV[i_edgePtr] = j + i_firstVsInClique;
                            i_edgePtr++;
                            tmpEdgeCounter[i][j]++;
                        }
                    }
                }
            }
        }
        int i_edgeStartCounter = 0;
        int i_edgeEndCounter = i_edgePtr;
        int edgeNum = i_edgePtr;
        int[] startVertex;
        int[] endVertex;
        if (glb.SCALE < 10) {
            numByte = 2 * edgeNum;
            startVertex = new int[numByte];
            endVertex = new int[numByte];
        } else {
            numByte = (edgeNum + glb.MAX_PARAL_EDGES * glb.TOT_VERTICES);
            startVertex = new int[numByte];
            endVertex = new int[numByte];
        }
        for (int i = i_edgeStartCounter; i < i_edgeEndCounter; i++) {
            startVertex[i] = startV[i - i_edgeStartCounter];
            endVertex[i] = endV[i - i_edgeStartCounter];
        }
        int numEdgesPlacedInCliques = edgeNum;
        i_edgePtr = 0;
        p = glb.PROB_INTERCL_EDGES;
        for (int i = 0; i < glb.TOT_VERTICES; i++) {
            int tempVertex1 = i;
            int h = totCliques;
            int l = 0;
            int t = -1;
            while (h - l > 1) {
                int m = (h + l) / 2;
                if (tempVertex1 >= firstVsInCliques[m]) {
                    l = m;
                } else {
                    if ((tempVertex1 < firstVsInCliques[m]) && (m > 0)) {
                        if (tempVertex1 >= firstVsInCliques[m - 1]) {
                            t = m - 1;
                            break;
                        } else {
                            h = m;
                        }
                    }
                }
            }
            if (t == -1) {
                int m;
                for (m = (l + 1); m < h; m++) {
                    if (tempVertex1 < firstVsInCliques[m]) {
                        break;
                    }
                }
                t = m - 1;
            }
            int t1 = firstVsInCliques[t];
            p = glb.PROB_INTERCL_EDGES;
            for (int d = 1; d < glb.TOT_VERTICES; d *= 2, p /= 2) {
                float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                if (r <= p) {
                    int tempVertex2 = (i + d) % glb.TOT_VERTICES;
                    h = totCliques;
                    l = 0;
                    t = -1;
                    while (h - l > 1) {
                        int m = (h + l) / 2;
                        if (tempVertex2 >= firstVsInCliques[m]) {
                            l = m;
                        } else {
                            if ((tempVertex2 < firstVsInCliques[m]) && (m > 0)) {
                                if (firstVsInCliques[m - 1] <= tempVertex2) {
                                    t = m - 1;
                                    break;
                                } else {
                                    h = m;
                                }
                            }
                        }
                    }
                    if (t == -1) {
                        int m;
                        for (m = (l + 1); m < h; m++) {
                            if (tempVertex2 < firstVsInCliques[m]) {
                                break;
                            }
                        }
                        t = m - 1;
                    }
                    int t2 = firstVsInCliques[t];
                    if (t1 != t2) {
                        int randNumEdges = (int) (randomPtr.posrandom_generate() % glb.MAX_PARAL_EDGES + 1);
                        for (int j = 0; j < randNumEdges; j++) {
                            startV[i_edgePtr] = tempVertex1;
                            endV[i_edgePtr] = tempVertex2;
                            i_edgePtr++;
                        }
                    }
                }
                float r0 = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                if ((r0 <= p) && (i - d >= 0)) {
                    int tempVertex2 = (i - d) % glb.TOT_VERTICES;
                    h = totCliques;
                    l = 0;
                    t = -1;
                    while (h - l > 1) {
                        int m = (h + l) / 2;
                        if (tempVertex2 >= firstVsInCliques[m]) {
                            l = m;
                        } else {
                            if ((tempVertex2 < firstVsInCliques[m]) && (m > 0)) {
                                if (firstVsInCliques[m - 1] <= tempVertex2) {
                                    t = m - 1;
                                    break;
                                } else {
                                    h = m;
                                }
                            }
                        }
                    }
                    if (t == -1) {
                        int m;
                        for (m = (l + 1); m < h; m++) {
                            if (tempVertex2 < firstVsInCliques[m]) {
                                break;
                            }
                        }
                        t = m - 1;
                    }
                    int t2 = firstVsInCliques[t];
                    if (t1 != t2) {
                        int randNumEdges = (int) (randomPtr.posrandom_generate() % glb.MAX_PARAL_EDGES + 1);
                        int j;
                        for (j = 0; j < randNumEdges; j++) {
                            startV[i_edgePtr] = tempVertex1;
                            endV[i_edgePtr] = tempVertex2;
                            i_edgePtr++;
                        }
                    }
                }
            }
        }
        i_edgeEndCounter = i_edgePtr;
        i_edgeStartCounter = 0;
        edgeNum = i_edgePtr;
        int numEdgesPlacedOutside = edgeNum;
        for (int i = i_edgeStartCounter; i < i_edgeEndCounter; i++) {
            startVertex[i + numEdgesPlacedInCliques] = startV[i - i_edgeStartCounter];
            int a = i + numEdgesPlacedInCliques;
            endVertex[i + numEdgesPlacedInCliques] = endV[i - i_edgeStartCounter];
        }
        int numEdgesPlaced = numEdgesPlacedInCliques + numEdgesPlacedOutside;
        SDGdataPtr.numEdgesPlaced = numEdgesPlaced;
        System.out.println("Finished generating edges");
        System.out.println("No. of intra-clique edges - " + numEdgesPlacedInCliques);
        System.out.println("No. of inter-clique edges - " + numEdgesPlacedOutside);
        System.out.println("Total no. of edges        - " + numEdgesPlaced);
        SDGdataPtr.intWeight = new int[numEdgesPlaced];
        p = glb.PERC_INT_WEIGHTS;
        int numStrWtEdges = 0;
        for (int i = 0; i < numEdgesPlaced; i++) {
            float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
            if (r <= p) {
                SDGdataPtr.intWeight[i] = (int) (1 + (randomPtr.posrandom_generate() % (glb.MAX_INT_WEIGHT - 1)));
            } else {
                SDGdataPtr.intWeight[i] = -1;
                numStrWtEdges++;
            }
        }
        {
            int t = 0;
            for (int i = 0; i < numEdgesPlaced; i++) {
                if (SDGdataPtr.intWeight[i] < 0) {
                    SDGdataPtr.intWeight[i] = -t;
                    t++;
                }
            }
        }
        SDGdataPtr.strWeight = new byte[numStrWtEdges * glb.MAX_STRLEN];
        for (int i = 0; i < numEdgesPlaced; i++) {
            if (SDGdataPtr.intWeight[i] <= 0) {
                for (int j = 0; j < glb.MAX_STRLEN; j++) {
                    SDGdataPtr.strWeight[(-SDGdataPtr.intWeight[i]) * glb.MAX_STRLEN + j] = (byte) (1 + randomPtr.posrandom_generate() % 127);
                }
            }
        }
        if (glb.SOUGHT_STRING.length != glb.MAX_STRLEN) {
            glb.SOUGHT_STRING = new byte[glb.MAX_STRLEN];
        }
        {
            int t = (int) (randomPtr.posrandom_generate() % numStrWtEdges);
            for (int j = 0; j < glb.MAX_STRLEN; j++) {
                glb.SOUGHT_STRING[j] = SDGdataPtr.strWeight[(t * glb.MAX_STRLEN + j)];
            }
        }
        for (int i = 0; i < numEdgesPlaced; i++) {
            startVertex[i] = permV[(startVertex[i])];
            endVertex[i] = permV[(endVertex[i])];
        }
        numByte = numEdgesPlaced;
        SDGdataPtr.startVertex = new int[numByte];
        SDGdataPtr.endVertex = new int[numByte];
        radixsort.all_radixsort_node_aux_s3_seq(numEdgesPlaced, startVertex, SDGdataPtr.startVertex, endVertex, SDGdataPtr.endVertex);
        if (glb.SCALE < 12) {
            int i0 = 0;
            int i1 = 0;
            int i = 0;
            while (i < numEdgesPlaced) {
                for (i = i0; i < numEdgesPlaced; i++) {
                    if (SDGdataPtr.startVertex[i] != SDGdataPtr.startVertex[i1]) {
                        i1 = i;
                        break;
                    }
                }
                for (int j = i0; j < i1; j++) {
                    for (int k = j + 1; k < i1; k++) {
                        if (SDGdataPtr.endVertex[k] < SDGdataPtr.endVertex[j]) {
                            int t = SDGdataPtr.endVertex[j];
                            SDGdataPtr.endVertex[j] = SDGdataPtr.endVertex[k];
                            SDGdataPtr.endVertex[k] = t;
                        }
                    }
                }
                if (SDGdataPtr.startVertex[i0] != glb.TOT_VERTICES - 1) {
                    i0 = i1;
                } else {
                    for (int j = i0; j < numEdgesPlaced; j++) {
                        for (int k = j + 1; k < numEdgesPlaced; k++) {
                            if (SDGdataPtr.endVertex[k] < SDGdataPtr.endVertex[j]) {
                                int t = SDGdataPtr.endVertex[j];
                                SDGdataPtr.endVertex[j] = SDGdataPtr.endVertex[k];
                                SDGdataPtr.endVertex[k] = t;
                            }
                        }
                    }
                }
            }
        } else {
            int[] tempIndex = new int[glb.TOT_VERTICES + 1];
            tempIndex[0] = 0;
            tempIndex[glb.TOT_VERTICES] = numEdgesPlaced;
            int i0 = 0;
            for (int i = 0; i < glb.TOT_VERTICES; i++) {
                tempIndex[i + 1] = tempIndex[i];
                for (int j = i0; j < numEdgesPlaced; j++) {
                    if (SDGdataPtr.startVertex[j] != SDGdataPtr.startVertex[i0]) {
                        if (SDGdataPtr.startVertex[i0] == i) {
                            tempIndex[i + 1] = j;
                            i0 = j;
                            break;
                        }
                    }
                }
            }
            for (int i = 0; i < glb.TOT_VERTICES; i++) {
                for (int j = tempIndex[i]; j < tempIndex[i + 1]; j++) {
                    for (int k = (j + 1); k < tempIndex[i + 1]; k++) {
                        if (SDGdataPtr.endVertex[k] < SDGdataPtr.endVertex[j]) {
                            int t = SDGdataPtr.endVertex[j];
                            SDGdataPtr.endVertex[j] = SDGdataPtr.endVertex[k];
                            SDGdataPtr.endVertex[k] = t;
                        }
                    }
                }
            }
        }
    }

    public static void genScalData(int myId, int numThread, Globals glb, GraphSDG SDGdataPtr, GenScalData gsd, Alg_Radix_Smp radixsort) {
        Random randomPtr = new Random();
        randomPtr.random_alloc();
        randomPtr.random_seed(myId);
        int[] permV;
        if (myId == 0) {
            permV = new int[glb.TOT_VERTICES];
            gsd.global_permV = permV;
        }
        Barrier.enterBarrier();
        permV = gsd.global_permV;
        LocalStartStop lss = new LocalStartStop();
        CreatePartition.createPartition(0, glb.TOT_VERTICES, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            permV[i] = i;
        }
        Barrier.enterBarrier();
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            int t1 = (int) (randomPtr.posrandom_generate());
            int t = i + t1 % (glb.TOT_VERTICES - i);
            if (t != i) {
                synchronized (common.G.lock) {
                    int t2 = permV[t];
                    permV[t] = permV[i];
                    permV[i] = t2;
                }
            }
        }
        int[] cliqueSizes;
        int estTotCliques = (int) (Math.ceil(1.5 * glb.TOT_VERTICES / ((1 + glb.MAX_CLIQUE_SIZE) / 2)));
        if (myId == 0) {
            cliqueSizes = new int[estTotCliques];
            gsd.global_cliqueSizes = cliqueSizes;
        }
        Barrier.enterBarrier();
        cliqueSizes = gsd.global_cliqueSizes;
        CreatePartition.createPartition(0, estTotCliques, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            cliqueSizes[i] = (int) (1 + (randomPtr.posrandom_generate() % glb.MAX_CLIQUE_SIZE));
        }
        Barrier.enterBarrier();
        int totCliques = 0;
        int[] lastVsInCliques;
        int[] firstVsInCliques;
        if (myId == 0) {
            lastVsInCliques = new int[estTotCliques];
            gsd.global_lastVsInCliques = lastVsInCliques;
            firstVsInCliques = new int[estTotCliques];
            gsd.global_firstVsInCliques = firstVsInCliques;
            lastVsInCliques[0] = cliqueSizes[0] - 1;
            int i;
            for (i = 1; i < estTotCliques; i++) {
                lastVsInCliques[i] = cliqueSizes[i] + lastVsInCliques[i - 1];
                if (lastVsInCliques[i] >= glb.TOT_VERTICES - 1) {
                    break;
                }
            }
            totCliques = i + 1;
            gsd.global_totCliques = totCliques;
            cliqueSizes[(totCliques - 1)] = glb.TOT_VERTICES - lastVsInCliques[(totCliques - 2)] - 1;
            lastVsInCliques[totCliques - 1] = glb.TOT_VERTICES - 1;
            firstVsInCliques[0] = 0;
        }
        Barrier.enterBarrier();
        lastVsInCliques = gsd.global_lastVsInCliques;
        firstVsInCliques = gsd.global_firstVsInCliques;
        totCliques = gsd.global_totCliques;
        CreatePartition.createPartition(1, totCliques, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            firstVsInCliques[i] = lastVsInCliques[i - 1] + 1;
        }
        int estTotEdges;
        if (glb.SCALE >= 12) {
            estTotEdges = (int) (Math.ceil(1.0d * ((glb.MAX_CLIQUE_SIZE - 1) * glb.TOT_VERTICES)));
        } else {
            estTotEdges = (int) (Math.ceil(1.2d * (((glb.MAX_CLIQUE_SIZE - 1) * glb.TOT_VERTICES) * ((1 + glb.MAX_PARAL_EDGES) / 2) + glb.TOT_VERTICES * 2)));
        }
        int i_edgePtr = 0;
        float p = glb.PROB_UNIDIRECTIONAL;
        int[] startV;
        int[] endV;
        if (numThread > 3) {
            int numByte = (int) (1.5 * (estTotEdges / numThread));
            startV = new int[numByte];
            endV = new int[numByte];
        } else {
            int numByte = (estTotEdges / numThread);
            startV = new int[numByte];
            endV = new int[numByte];
        }
        int[][] tmpEdgeCounter = new int[glb.MAX_CLIQUE_SIZE][glb.MAX_CLIQUE_SIZE];
        CreatePartition.createPartition(0, totCliques, myId, numThread, lss);
        for (int i_clique = lss.i_start; i_clique < lss.i_stop; i_clique++) {
            int i_cliqueSize = cliqueSizes[i_clique];
            int i_firstVsInClique = firstVsInCliques[i_clique];
            for (int i = 0; i < i_cliqueSize; i++) {
                for (int j = 0; j < i; j++) {
                    float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                    if (r >= p) {
                        startV[i_edgePtr] = i + i_firstVsInClique;
                        endV[i_edgePtr] = j + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[i][j] = 1;
                        startV[i_edgePtr] = j + i_firstVsInClique;
                        endV[i_edgePtr] = i + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[j][i] = 1;
                    } else if (r >= 0.5) {
                        startV[i_edgePtr] = i + i_firstVsInClique;
                        endV[i_edgePtr] = j + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[i][j] = 1;
                        tmpEdgeCounter[j][i] = 0;
                    } else {
                        startV[i_edgePtr] = j + i_firstVsInClique;
                        endV[i_edgePtr] = i + i_firstVsInClique;
                        i_edgePtr++;
                        tmpEdgeCounter[j][i] = 1;
                        tmpEdgeCounter[i][j] = 0;
                    }
                }
            }
            if (i_cliqueSize != 1) {
                int randNumEdges = (int) (randomPtr.posrandom_generate() % (2 * i_cliqueSize * glb.MAX_PARAL_EDGES));
                for (int i_paralEdge = 0; i_paralEdge < randNumEdges; i_paralEdge++) {
                    int i = (int) (randomPtr.posrandom_generate() % i_cliqueSize);
                    int j = (int) (randomPtr.posrandom_generate() % i_cliqueSize);
                    if ((i != j) && (tmpEdgeCounter[i][j] < glb.MAX_PARAL_EDGES)) {
                        float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                        if (r >= p) {
                            startV[i_edgePtr] = i + i_firstVsInClique;
                            endV[i_edgePtr] = j + i_firstVsInClique;
                            i_edgePtr++;
                            tmpEdgeCounter[i][j]++;
                        }
                    }
                }
            }
        }
        tmpEdgeCounter = null;
        int[] i_edgeStartCounter;
        int[] i_edgeEndCounter;
        if (myId == 0) {
            i_edgeStartCounter = new int[numThread];
            gsd.global_i_edgeStartCounter = i_edgeStartCounter;
            i_edgeEndCounter = new int[numThread];
            gsd.global_i_edgeEndCounter = i_edgeEndCounter;
        }
        Barrier.enterBarrier();
        i_edgeStartCounter = gsd.global_i_edgeStartCounter;
        i_edgeEndCounter = gsd.global_i_edgeEndCounter;
        i_edgeEndCounter[myId] = i_edgePtr;
        i_edgeStartCounter[myId] = 0;
        Barrier.enterBarrier();
        if (myId == 0) {
            for (int i = 1; i < numThread; i++) {
                i_edgeEndCounter[i] = i_edgeEndCounter[i - 1] + i_edgeEndCounter[i];
                i_edgeStartCounter[i] = i_edgeEndCounter[i - 1];
            }
        }
        synchronized (common.G.lock) {
            gsd.global_edgeNum = gsd.global_edgeNum + i_edgePtr;
        }
        Barrier.enterBarrier();
        int edgeNum = gsd.global_edgeNum;
        int[] startVertex;
        int[] endVertex;
        if (myId == 0) {
            if (glb.SCALE < 10) {
                int numByte = 2 * edgeNum;
                startVertex = new int[numByte];
                endVertex = new int[numByte];
            } else {
                int numByte = (edgeNum + glb.MAX_PARAL_EDGES * glb.TOT_VERTICES);
                startVertex = new int[numByte];
                endVertex = new int[numByte];
            }
            gsd.global_startVertex = startVertex;
            gsd.global_endVertex = endVertex;
        }
        Barrier.enterBarrier();
        startVertex = gsd.global_startVertex;
        endVertex = gsd.global_endVertex;
        for (int i = i_edgeStartCounter[myId]; i < i_edgeEndCounter[myId]; i++) {
            startVertex[i] = startV[i - i_edgeStartCounter[myId]];
            endVertex[i] = endV[i - i_edgeStartCounter[myId]];
        }
        int numEdgesPlacedInCliques = edgeNum;
        Barrier.enterBarrier();
        i_edgePtr = 0;
        p = glb.PROB_INTERCL_EDGES;
        CreatePartition.createPartition(0, glb.TOT_VERTICES, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            int tempVertex1 = i;
            int h = totCliques;
            int l = 0;
            int t = -1;
            while (h - l > 1) {
                int m = ((h + l) / 2);
                if (tempVertex1 >= firstVsInCliques[m]) {
                    l = m;
                } else {
                    if ((tempVertex1 < firstVsInCliques[m]) && (m > 0)) {
                        if (tempVertex1 >= firstVsInCliques[m - 1]) {
                            t = m - 1;
                            break;
                        } else {
                            h = m;
                        }
                    }
                }
            }
            if (t == -1) {
                int m;
                for (m = (l + 1); m < h; m++) {
                    if (tempVertex1 < firstVsInCliques[m]) {
                        break;
                    }
                }
                t = m - 1;
            }
            int t1 = firstVsInCliques[t];
            p = glb.PROB_INTERCL_EDGES;
            for (int d = 1; d < glb.TOT_VERTICES; d *= 2, p /= 2) {
                float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                if (r <= p) {
                    int tempVertex2 = ((i + d) % glb.TOT_VERTICES);
                    h = totCliques;
                    l = 0;
                    t = -1;
                    while (h - l > 1) {
                        int m = (h + l) / 2;
                        if (tempVertex2 >= firstVsInCliques[m]) {
                            l = m;
                        } else {
                            if ((tempVertex2 < firstVsInCliques[m]) && (m > 0)) {
                                if (firstVsInCliques[m - 1] <= tempVertex2) {
                                    t = m - 1;
                                    break;
                                } else {
                                    h = m;
                                }
                            }
                        }
                    }
                    if (t == -1) {
                        int m;
                        for (m = (l + 1); m < h; m++) {
                            if (tempVertex2 < firstVsInCliques[m]) {
                                break;
                            }
                        }
                        t = m - 1;
                    }
                    int t2 = firstVsInCliques[t];
                    if (t1 != t2) {
                        int randNumEdges = (int) (randomPtr.posrandom_generate() % glb.MAX_PARAL_EDGES + 1);
                        for (int j = 0; j < randNumEdges; j++) {
                            startV[i_edgePtr] = tempVertex1;
                            endV[i_edgePtr] = tempVertex2;
                            i_edgePtr++;
                        }
                    }
                }
                float r0 = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
                if ((r0 <= p) && (i - d >= 0)) {
                    int tempVertex2 = (i - d) % glb.TOT_VERTICES;
                    h = totCliques;
                    l = 0;
                    t = -1;
                    while (h - l > 1) {
                        int m = ((h + l) / 2);
                        if (tempVertex2 >= firstVsInCliques[m]) {
                            l = m;
                        } else {
                            if ((tempVertex2 < firstVsInCliques[m]) && (m > 0)) {
                                if (firstVsInCliques[m - 1] <= tempVertex2) {
                                    t = m - 1;
                                    break;
                                } else {
                                    h = m;
                                }
                            }
                        }
                    }
                    if (t == -1) {
                        int m;
                        for (m = (l + 1); m < h; m++) {
                            if (tempVertex2 < firstVsInCliques[m]) {
                                break;
                            }
                        }
                        t = m - 1;
                    }
                    int t2 = firstVsInCliques[t];
                    if (t1 != t2) {
                        int randNumEdges = (int) (randomPtr.posrandom_generate() % glb.MAX_PARAL_EDGES + 1);
                        for (int j = 0; j < randNumEdges; j++) {
                            startV[i_edgePtr] = tempVertex1;
                            endV[i_edgePtr] = tempVertex2;
                            i_edgePtr++;
                        }
                    }
                }
            }
        }
        i_edgeEndCounter[myId] = i_edgePtr;
        i_edgeStartCounter[myId] = 0;
        if (myId == 0) {
            gsd.global_edgeNum = 0;
        }
        Barrier.enterBarrier();
        if (myId == 0) {
            for (int i = 1; i < numThread; i++) {
                i_edgeEndCounter[i] = i_edgeEndCounter[i - 1] + i_edgeEndCounter[i];
                i_edgeStartCounter[i] = i_edgeEndCounter[i - 1];
            }
        }
        synchronized (common.G.lock) {
            gsd.global_edgeNum = gsd.global_edgeNum + i_edgePtr;
        }
        Barrier.enterBarrier();
        edgeNum = gsd.global_edgeNum;
        int numEdgesPlacedOutside = gsd.global_edgeNum;
        for (int i = i_edgeStartCounter[myId]; i < i_edgeEndCounter[myId]; i++) {
            startVertex[i + numEdgesPlacedInCliques] = startV[i - i_edgeStartCounter[myId]];
            endVertex[i + numEdgesPlacedInCliques] = endV[i - i_edgeStartCounter[myId]];
        }
        Barrier.enterBarrier();
        int numEdgesPlaced = numEdgesPlacedInCliques + numEdgesPlacedOutside;
        if (myId == 0) {
            SDGdataPtr.numEdgesPlaced = numEdgesPlaced;
            System.out.println("Finished generating edges");
            System.out.println("No. of intra-clique edges - " + numEdgesPlacedInCliques);
            System.out.println("No. of inter-clique edges - " + numEdgesPlacedOutside);
            System.out.println("Total no. of edges        - " + numEdgesPlaced);
        }
        Barrier.enterBarrier();
        if (myId == 0) {
            SDGdataPtr.intWeight = new int[numEdgesPlaced];
        }
        Barrier.enterBarrier();
        p = glb.PERC_INT_WEIGHTS;
        int numStrWtEdges = 0;
        CreatePartition.createPartition(0, numEdgesPlaced, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            float r = (float) (randomPtr.posrandom_generate() % 1000) / (float) 1000;
            if (r <= p) {
                SDGdataPtr.intWeight[i] = (int) (1 + (randomPtr.posrandom_generate() % (glb.MAX_INT_WEIGHT - 1)));
            } else {
                SDGdataPtr.intWeight[i] = -1;
                numStrWtEdges++;
            }
        }
        Barrier.enterBarrier();
        if (myId == 0) {
            int t = 0;
            for (int i = 0; i < numEdgesPlaced; i++) {
                if (SDGdataPtr.intWeight[i] < 0) {
                    SDGdataPtr.intWeight[i] = -t;
                    t++;
                }
            }
        }
        synchronized (common.G.lock) {
            gsd.global_numStrWtEdges = gsd.global_numStrWtEdges + numStrWtEdges;
        }
        Barrier.enterBarrier();
        numStrWtEdges = gsd.global_numStrWtEdges;
        if (myId == 0) {
            SDGdataPtr.strWeight = new byte[numStrWtEdges * glb.MAX_STRLEN];
        }
        Barrier.enterBarrier();
        CreatePartition.createPartition(0, numEdgesPlaced, myId, numThread, lss);
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            if (SDGdataPtr.intWeight[i] <= 0) {
                for (int j = 0; j < glb.MAX_STRLEN; j++) {
                    SDGdataPtr.strWeight[(-SDGdataPtr.intWeight[i]) * glb.MAX_STRLEN + j] = (byte) (1 + (randomPtr.posrandom_generate() % 127));
                }
            }
        }
        if (myId == 0) {
            if (glb.SOUGHT_STRING.length != glb.MAX_STRLEN) {
                glb.SOUGHT_STRING = new byte[glb.MAX_STRLEN];
            }
            int t = (int) (randomPtr.posrandom_generate() % numStrWtEdges);
            for (int j = 0; j < glb.MAX_STRLEN; j++) {
                glb.SOUGHT_STRING[j] = SDGdataPtr.strWeight[(t * glb.MAX_STRLEN + j)];
            }
        }
        Barrier.enterBarrier();
        for (int i = lss.i_start; i < lss.i_stop; i++) {
            startVertex[i] = permV[startVertex[i]];
            endVertex[i] = permV[endVertex[i]];
        }
        Barrier.enterBarrier();
        if (myId == 0) {
            int numByte = numEdgesPlaced;
            SDGdataPtr.startVertex = new int[numByte];
            SDGdataPtr.endVertex = new int[numByte];
        }
        Barrier.enterBarrier();
        Alg_Radix_Smp.all_radixsort_node_aux_s3(myId, numThread, numEdgesPlaced, startVertex, SDGdataPtr.startVertex, endVertex, SDGdataPtr.endVertex, radixsort);
        Barrier.enterBarrier();
        if (glb.SCALE < 12) {
            if (myId == 0) {
                int i0 = 0;
                int i1 = 0;
                int i = 0;
                while (i < numEdgesPlaced) {
                    for (i = i0; i < numEdgesPlaced; i++) {
                        if (SDGdataPtr.startVertex[i] != SDGdataPtr.startVertex[i1]) {
                            i1 = i;
                            break;
                        }
                    }
                    for (int j = i0; j < i1; j++) {
                        for (int k = j + 1; k < i1; k++) {
                            if (SDGdataPtr.endVertex[k] < SDGdataPtr.endVertex[j]) {
                                int t = SDGdataPtr.endVertex[j];
                                SDGdataPtr.endVertex[j] = SDGdataPtr.endVertex[k];
                                SDGdataPtr.endVertex[k] = t;
                            }
                        }
                    }
                    if (SDGdataPtr.startVertex[i0] != glb.TOT_VERTICES - 1) {
                        i0 = i1;
                    } else {
                        for (int j = i0; j < numEdgesPlaced; j++) {
                            for (int k = j + 1; k < numEdgesPlaced; k++) {
                                if (SDGdataPtr.endVertex[k] < SDGdataPtr.endVertex[j]) {
                                    int t = SDGdataPtr.endVertex[j];
                                    SDGdataPtr.endVertex[j] = SDGdataPtr.endVertex[k];
                                    SDGdataPtr.endVertex[k] = t;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            int[] tempIndex;
            if (myId == 0) {
                tempIndex = new int[glb.TOT_VERTICES + 1];
                gsd.global_tempIndex = tempIndex;
                tempIndex[0] = 0;
                tempIndex[glb.TOT_VERTICES] = numEdgesPlaced;
                int i0 = 0;
                for (int i = 0; i < glb.TOT_VERTICES; i++) {
                    tempIndex[i + 1] = tempIndex[i];
                    for (int j = i0; j < numEdgesPlaced; j++) {
                        if (SDGdataPtr.startVertex[j] != SDGdataPtr.startVertex[i0]) {
                            if (SDGdataPtr.startVertex[i0] == i) {
                                tempIndex[i + 1] = j;
                                i0 = j;
                                break;
                            }
                        }
                    }
                }
            }
            Barrier.enterBarrier();
            tempIndex = gsd.global_tempIndex;
            if (myId == 0) {
                for (int i = 0; i < glb.TOT_VERTICES; i++) {
                    for (int j = tempIndex[i]; j < tempIndex[i + 1]; j++) {
                        for (int k = (j + 1); k < tempIndex[i + 1]; k++) {
                            if (SDGdataPtr.endVertex[k] < SDGdataPtr.endVertex[j]) {
                                int t = SDGdataPtr.endVertex[j];
                                SDGdataPtr.endVertex[j] = SDGdataPtr.endVertex[k];
                                SDGdataPtr.endVertex[k] = t;
                            }
                        }
                    }
                }
            }
        }
    }
}
