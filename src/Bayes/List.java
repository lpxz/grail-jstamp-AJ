package Bayes;

public class List {

    private static final boolean LIST_NO_DUPLICATES = false;

    /*atomicset(L)*/
    /*atomic(L)*/public ListNode head /*N=this.L*/;

    /*atomic(L)*/public int size;

    public List() {
    }

    public static int compareTask(LearnerTask aPtr, LearnerTask bPtr) {
        LearnerTask aTaskPtr = (LearnerTask) aPtr;
        LearnerTask bTaskPtr = (LearnerTask) bPtr;
        float aScore = aTaskPtr.score;
        float bScore = bTaskPtr.score;
        if (aScore < bScore) {
            return 1;
        } else if (aScore > bScore) {
            return -1;
        } else {
            return (aTaskPtr.toId - bTaskPtr.toId);
        }
    }

    public void list_iter_reset(ListIter itPtr) {
        itPtr.ptr = head;
    }

    public boolean list_iter_hasNext(ListIter itPtr) {
        return itPtr.ptr.nextPtr != null;
    }

    public LearnerTask list_iter_next(ListIter itPtr) {
        LearnerTask lt = itPtr.ptr.dataPtr;
        itPtr.ptr = itPtr.ptr.nextPtr;
        return lt;
    }

    public ListNode allocNode(LearnerTask dataPtr) {
        ListNode nodePtr = new ListNode();
        nodePtr.dataPtr = dataPtr;
        nodePtr.nextPtr = null;
        return nodePtr;
    }

    public static List list_alloc() {
        List listPtr = new List();
        listPtr.head = new ListNode();
        listPtr.head.dataPtr = null;
        listPtr.head.nextPtr = null;
        listPtr.size = 0;
        return listPtr;
    }

    public void freeNode(ListNode nodePtr) {
        nodePtr = null;
    }

    public void freeList(ListNode nodePtr) {
        if (nodePtr != null) {
            freeList(nodePtr.nextPtr);
            freeNode(nodePtr);
        }
    }

    public void list_free() {
        freeList(head.nextPtr);
    }

    public boolean list_isEmpty() {
        return (head.nextPtr == null);
    }

    public int list_getSize() {
        return size;
    }

    public ListNode findPrevious(LearnerTask dataPtr) {
        ListNode prevPtr = head;
        ListNode nodePtr = prevPtr.nextPtr;
        for (; nodePtr != null; nodePtr = nodePtr.nextPtr) {
            if (compareTask(nodePtr.dataPtr, dataPtr) >= 0) {
                return prevPtr;
            }
            prevPtr = nodePtr;
        }
        return prevPtr;
    }

    public LearnerTask list_find(LearnerTask dataPtr) {
        ListNode nodePtr;
        ListNode prevPtr = findPrevious(dataPtr);
        nodePtr = prevPtr.nextPtr;
        if ((nodePtr == null) || (compareTask(nodePtr.dataPtr, dataPtr) != 0)) {
            return null;
        }
        return (nodePtr.dataPtr);
    }

    public boolean list_insert(LearnerTask dataPtr) {
        ListNode prevPtr;
        ListNode nodePtr;
        ListNode currPtr;
        prevPtr = findPrevious(dataPtr);
        currPtr = prevPtr.nextPtr;
        if (LIST_NO_DUPLICATES) {
            if ((currPtr != null) && compareTask(currPtr.dataPtr, dataPtr) == 0) {
                return false;
            }
        }
        nodePtr = allocNode(dataPtr);
        nodePtr.nextPtr = currPtr;
        prevPtr.nextPtr = nodePtr;
        size++;
        return true;
    }

    public boolean list_remove(LearnerTask dataPtr) {
        ListNode prevPtr;
        ListNode nodePtr;
        prevPtr = findPrevious(dataPtr);
        nodePtr = prevPtr.nextPtr;
        if ((nodePtr != null) && (compareTask(nodePtr.dataPtr, dataPtr) == 0)) {
            prevPtr.nextPtr = nodePtr.nextPtr;
            nodePtr.nextPtr = null;
            freeNode(nodePtr);
            size--;
            return true;
        }
        return false;
    }

    public void list_clear() {
        freeList(head.nextPtr);
        head.nextPtr = null;
        size = 0;
    }
}
