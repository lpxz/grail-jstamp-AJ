package Bayes;

public class IntList {

    private static final boolean LIST_NO_DUPLICATES = false;

    public IntListNode head;

    public int size;

    public IntList() {
    }

    public void list_iter_reset(IntListIter itPtr) {
        itPtr.ptr = head;
    }

    public boolean list_iter_hasNext(IntListIter itPtr) {
        return (itPtr.ptr.nextPtr != null);
    }

    public int list_iter_next(IntListIter itPtr) {
        int val = itPtr.ptr.dataPtr;
        itPtr.ptr = itPtr.ptr.nextPtr;
        return val;
    }

    public IntListNode allocNode(int dataPtr) {
        IntListNode nodePtr = new IntListNode();
        nodePtr.dataPtr = dataPtr;
        nodePtr.nextPtr = null;
        return nodePtr;
    }

    public static IntList list_alloc() {
        IntList listPtr = new IntList();
        listPtr.head = new IntListNode();
        listPtr.head.dataPtr = 0;
        listPtr.head.nextPtr = null;
        listPtr.size = 0;
        return listPtr;
    }

    public void freeNode(IntListNode nodePtr) {
        nodePtr = null;
    }

    public void freeList(IntListNode nodePtr) {
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

    public IntListNode findPrevious(int dataPtr) {
        IntListNode prevPtr = head;
        IntListNode nodePtr = prevPtr.nextPtr;
        for (; nodePtr != null; nodePtr = nodePtr.nextPtr) {
            if (compareId(nodePtr.dataPtr, dataPtr) >= 0) {
                return prevPtr;
            }
            prevPtr = nodePtr;
        }
        return prevPtr;
    }

    public boolean list_insert(int dataPtr) {
        IntListNode prevPtr;
        IntListNode nodePtr;
        IntListNode currPtr;
        prevPtr = findPrevious(dataPtr);
        currPtr = prevPtr.nextPtr;
        if (LIST_NO_DUPLICATES) {
            if ((currPtr != null) && compareId(currPtr.dataPtr, dataPtr) == 0) {
                return false;
            }
        }
        nodePtr = allocNode(dataPtr);
        nodePtr.nextPtr = currPtr;
        prevPtr.nextPtr = nodePtr;
        size++;
        return true;
    }

    public static int compareId(int a, int b) {
        return (a - b);
    }

    public boolean list_remove(int dataPtr) {
        IntListNode prevPtr;
        IntListNode nodePtr;
        prevPtr = findPrevious(dataPtr);
        nodePtr = prevPtr.nextPtr;
        if ((nodePtr != null) && (compareId(nodePtr.dataPtr, dataPtr) == 0)) {
            prevPtr.nextPtr = nodePtr.nextPtr;
            nodePtr.nextPtr = null;
            freeNode(nodePtr);
            size--;
            return true;
        }
        return false;
    }
}
