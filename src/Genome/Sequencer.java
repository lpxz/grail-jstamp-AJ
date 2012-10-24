package Genome;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import Yada.java.Barrier;
import common.Random;

public class Sequencer {
  public ByteString sequence;

    public Segments segmentsPtr;

    Hashtable uniqueSegmentsPtr;

    endInfoEntry endInfoEntries[];

    Table startHashToConstructEntryTables[];

    constructEntry constructEntries[];

    Table hashToConstructEntryTable;

    int segmentLength;

    public Sequencer(int myGeneLength, int mySegmentLength, Segments mySegmentsPtr) {
        int maxNumUniqueSegment = myGeneLength - mySegmentLength + 1;
        int i;
        uniqueSegmentsPtr = new Hashtable((int) myGeneLength, -1, -1);
        endInfoEntries = new endInfoEntry[maxNumUniqueSegment];
        for (i = 0; i < maxNumUniqueSegment; i++) {
            endInfoEntries[i] = new endInfoEntry(true, 1);
        }
        startHashToConstructEntryTables = new Table[mySegmentLength];
        for (i = 1; i < mySegmentLength; i++) {
            startHashToConstructEntryTables[i] = new Table(myGeneLength);
        }
        segmentLength = mySegmentLength;
        constructEntries = new constructEntry[maxNumUniqueSegment];
        for (i = 0; i < maxNumUniqueSegment; i++) {
            constructEntries[i] = new constructEntry(null, true, 0, null, null, null, 0, segmentLength);
        }
        hashToConstructEntryTable = new Table(myGeneLength);
        segmentsPtr = mySegmentsPtr;
    }

    public static void run(int threadNum, int numOfThreads, Random randomPtr, Sequencer sequencerPtr) {
        int threadId = threadNum;
        Segments segmentsPtr = sequencerPtr.segmentsPtr;
        Hashtable uniqueSegmentsPtr = sequencerPtr.uniqueSegmentsPtr;
        endInfoEntry endInfoEntries[] = sequencerPtr.endInfoEntries;
        Table startHashToConstructEntryTables[] = sequencerPtr.startHashToConstructEntryTables;
        constructEntry constructEntries[] = sequencerPtr.constructEntries;
        Table hashToConstructEntryTable = sequencerPtr.hashToConstructEntryTable;
        Vector segmentsContentsPtr = segmentsPtr.contentsPtr;
        int numSegment = segmentsContentsPtr.size();
        int segmentLength = segmentsPtr.length;
        int i;
        int j;
        int i_start;
        int i_stop;
        int numUniqueSegment;
        int substringLength;
        int entryIndex;
        int CHUNK_STEP1 = 12;
        int numThread = numOfThreads;
        {
            int partitionSize = (numSegment + numThread / 2) / numThread;
            i_start = threadId * partitionSize;
            if (threadId == (numThread - 1)) {
                i_stop = numSegment;
            } else {
                i_stop = i_start + partitionSize;
            }
        }
        for (i = i_start; i < i_stop; i += CHUNK_STEP1) {
            synchronized (common.G.lock) {
                int ii;
                int ii_stop = Math.min(i_stop, (i + CHUNK_STEP1));
                for (ii = i; ii < ii_stop; ii++) {
                    ByteString segment = (ByteString) segmentsContentsPtr.elementAt(ii);
                    if (!uniqueSegmentsPtr.TMhashtable_insert(segment, segment)) {
                        ;
                    }
                }
            }
        }
        Barrier.enterBarrier();
        numUniqueSegment = uniqueSegmentsPtr.size;
        entryIndex = 0;
        {
            int num = uniqueSegmentsPtr.numBucket;
            int partitionSize = (num + numThread / 2) / numThread;
            i_start = threadId * partitionSize;
            if (threadId == (numThread - 1)) {
                i_stop = num;
            } else {
                i_stop = i_start + partitionSize;
            }
        }
        {
            int partitionSize = (numUniqueSegment + numThread / 2) / numThread;
            entryIndex = threadId * partitionSize;
        }
        for (i = i_start; i < i_stop; i++) {
            List chainPtr = uniqueSegmentsPtr.buckets[i];
            ListNode it = chainPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                ByteString segment = it.dataPtr.firstPtr;
                int newj;
                int startHash;
                boolean status;
                synchronized (common.G.lock) {
                    while (constructEntries[entryIndex].segment != null) {
                        entryIndex = (entryIndex + 1) % numUniqueSegment;
                    }
                    constructEntries[entryIndex].segment = segment;
                }
                constructEntry constructEntryPtr = constructEntries[entryIndex];
                entryIndex = (entryIndex + 1) % numUniqueSegment;
                constructEntryPtr.endHash = segment.substring(1).hashCode();
                startHash = 0;
                for (newj = 1; newj < segmentLength; newj++) {
                    startHash = segment.byteAt(newj - 1) + (startHash << 6) + (startHash << 16) - startHash;
                    synchronized (common.G.lock) {
                        boolean check = startHashToConstructEntryTables[newj].table_insert(startHash, constructEntryPtr);
                    }
                }
                startHash = segment.byteAt(newj - 1) + (startHash << 6) + (startHash << 16) - startHash;
                synchronized (common.G.lock) {
                    hashToConstructEntryTable.table_insert(startHash, constructEntryPtr);
                }
            }
        }
        Barrier.enterBarrier();
        for (substringLength = segmentLength - 1; substringLength > 0; substringLength--) {
            Table startHashToConstructEntryTablePtr = startHashToConstructEntryTables[substringLength];
            LinkedList buckets[] = startHashToConstructEntryTablePtr.buckets;
            int numBucket = startHashToConstructEntryTablePtr.numBucket;
            int index_start;
            int index_stop;
            {
                int partitionSize = (numUniqueSegment + numThread / 2) / numThread;
                index_start = threadId * partitionSize;
                if (threadId == (numThread - 1)) {
                    index_stop = numUniqueSegment;
                } else {
                    index_stop = index_start + partitionSize;
                }
            }
            for (entryIndex = index_start; entryIndex < index_stop; entryIndex += endInfoEntries[entryIndex].jumpToNext) {
                if (!endInfoEntries[entryIndex].isEnd) {
                    continue;
                }
                constructEntry endConstructEntryPtr = constructEntries[entryIndex];
                ByteString endSegment = endConstructEntryPtr.segment;
                int endHash = endConstructEntryPtr.endHash;
                LinkedList chainPtr = buckets[(endHash % numBucket)];
                ListIterator it = (ListIterator) chainPtr.iterator();
                while (it.hasNext()) {
                    constructEntry startConstructEntryPtr = (constructEntry) it.next(); if(startConstructEntryPtr==null) continue;
                    ByteString startSegment = startConstructEntryPtr.segment;
                    synchronized (common.G.lock) {
                        trans2(startSegment, endSegment, startConstructEntryPtr, endConstructEntryPtr, segmentLength, substringLength, endInfoEntries, entryIndex);
                    }
                    if (!endInfoEntries[entryIndex].isEnd) {
                        break;
                    }
                }
            }
            Barrier.enterBarrier();
            if (threadId == 0) {
                if (substringLength > 1) {
                    int index = segmentLength - substringLength + 1;
                    for (i = 1; !endInfoEntries[i].isEnd; i += endInfoEntries[i].jumpToNext) {
                        ;
                    }
                    endInfoEntries[0].jumpToNext = i;
                    if (endInfoEntries[0].isEnd) {
                        ByteString segment = constructEntries[0].segment;
                        constructEntries[0].endHash = segment.subString(index).hashCode();
                    }
                    for (j = 0; i < numUniqueSegment; i += endInfoEntries[i].jumpToNext) {
                        if (endInfoEntries[i].isEnd) {
                            ByteString segment = constructEntries[i].segment;
                           if(segment!=null) constructEntries[i].endHash = segment.substring(index).hashCode();
                            endInfoEntries[j].jumpToNext = Math.max(1, i - j);
                            j = i;
                        }
                    }
                    endInfoEntries[j].jumpToNext = i - j;
                }
            }
            Barrier.enterBarrier();
        }
        Barrier.enterBarrier();
        if (threadId == 0) {
            int totalLength = 0;
            for (i = 0; i < numUniqueSegment; i++) {
                if (constructEntries[i].isStart) {
                    totalLength += constructEntries[i].length;
                }
            }
            ByteString sequence = sequencerPtr.sequence;
            ByteString copyPtr = sequence;
            int sequenceLength = 0;
            for (i = 0; i < numUniqueSegment; i++) {
                constructEntry constructEntryPtr = constructEntries[i];
                if (constructEntryPtr.isStart) {
                    int newSequenceLength = sequenceLength + constructEntryPtr.length;
                    int prevOverlap = 0;
                    do {
                        int numChar = segmentLength - constructEntryPtr.overlap;
                        copyPtr = constructEntryPtr.segment;
                        if (sequencerPtr.sequence == null) {
                            sequencerPtr.sequence = copyPtr;
                        } else {
                        	 if(copyPtr!=null) sequencerPtr.sequence = sequencerPtr.sequence.concat(copyPtr.substring(prevOverlap));
                        }
                        prevOverlap = constructEntryPtr.overlap;
                        constructEntryPtr = constructEntryPtr.nextPtr;
                    } while (constructEntryPtr != null);
                }
            }
        }
    }

    static void trans2(ByteString /*unitfor(Y)*/startSegment, ByteString /*unitfor(Y)*/endSegment, constructEntry /*unitfor(C)*/ startConstructEntryPtr, constructEntry  /*unitfor(C)*/ endConstructEntryPtr, int segmentLength, int substringLength, endInfoEntry endInfoEntries[], int entryIndex) {
        if (startConstructEntryPtr.isStart && (endConstructEntryPtr.startPtr != startConstructEntryPtr) && (startSegment.substring(0, substringLength).compareTo(endSegment.substring(segmentLength - substringLength)) == 0)) {
            startConstructEntryPtr.isStart = false;
            endInfoEntries[entryIndex].isEnd = false;
            constructEntry startConstructEntry_endPtr = startConstructEntryPtr.endPtr;
            constructEntry endConstructEntry_startPtr = endConstructEntryPtr.startPtr;
            startConstructEntry_endPtr.startPtr = endConstructEntry_startPtr;
            endConstructEntryPtr.nextPtr = startConstructEntryPtr;
            endConstructEntry_startPtr.endPtr = startConstructEntry_endPtr;
            endConstructEntryPtr.overlap = substringLength;
            int newLength = endConstructEntry_startPtr.length + startConstructEntryPtr.length - substringLength;
            endConstructEntry_startPtr.length = newLength;
        }
    }
}
