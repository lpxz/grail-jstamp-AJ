package Yada.java;

public class List_t {

    public List_Node head;

    int size;

    int mode;

    public List_t() {
        head = new List_Node();
    }

    private List_Node allocNode(Object dataPtr) {
        List_Node nodePtr = new List_Node();
        nodePtr.dataPtr = dataPtr;
        nodePtr.nextPtr = null;
        return nodePtr;
    }

    public List_t(int mode) {
        head = new List_Node();
        this.mode = mode;
        head.dataPtr = null;
        head.nextPtr = null;
        size = 0;
    }

    public boolean isEmpty() {
        return (head.nextPtr == null);
    }

    public int getSize() {
        return size;
    }

    private List_Node findPrevious(Object dataPtr) {
        List_Node prevPtr = head;
        List_Node nodePtr = prevPtr.nextPtr;
        for (; nodePtr != null; nodePtr = nodePtr.nextPtr) {
            if (compare(nodePtr.dataPtr, dataPtr) >= 0) {
                return prevPtr;
            }
            prevPtr = nodePtr;
        }
        return prevPtr;
    }

    public Object find(Object dataPtr) {
        List_Node nodePtr;
        List_Node prevPtr = findPrevious(dataPtr);
        nodePtr = prevPtr.nextPtr;
        if ((nodePtr == null) || (compare(nodePtr.dataPtr, dataPtr) != 0)) {
            return null;
        }
        return nodePtr.dataPtr;
    }

    public int compare(Object obj1, Object obj2) {
        if (mode == 0) {
            return element.element_compare((element) obj1, (element) obj2);
        } else {
            return element.compareEdge((edge) obj1, (edge) obj2);
        }
    }

    public boolean insert(Object dataPtr) {
        List_Node prevPtr;
        List_Node nodePtr;
        List_Node currPtr;
        prevPtr = findPrevious(dataPtr);
        currPtr = prevPtr.nextPtr;
        if ((currPtr != null) && compare(currPtr.dataPtr, dataPtr) == 0) {
            return false;
        }
        nodePtr = allocNode(dataPtr);
        if (nodePtr == null) {
            return false;
        }
        nodePtr.nextPtr = currPtr;
        prevPtr.nextPtr = nodePtr;
        size++;
        return true;
    }

    public boolean remove(Object dataPtr) {
        List_Node prevPtr;
        List_Node nodePtr;
        prevPtr = findPrevious(dataPtr);
        nodePtr = prevPtr.nextPtr;
        if ((nodePtr != null) && (compare(nodePtr.dataPtr, dataPtr) == 0)) {
            prevPtr.nextPtr = nodePtr.nextPtr;
            nodePtr.nextPtr = null;
            size--;
            return true;
        }
        return false;
    }

    int compareObject(Object obj1, Object obj2) {
        return 1;
    }

    public void clear() {
        head = new List_Node();
        size = 0;
    }

    public static void main(String[] argv) {
        List_t listPtr;
        int[] data1 = new int[5];
        int[] data2 = new int[6];
        int i;
        System.out.println("Starting...");
    }
}
