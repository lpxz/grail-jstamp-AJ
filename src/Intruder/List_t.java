package Intruder;

public class List_t {

    List_Node head;

    int chk;

    int size;

    public List_t() {
        head = new List_Node();
    }

    private List_Node allocNode(Object dataPtr) {
        List_Node nodePtr = new List_Node();
        nodePtr.dataPtr = dataPtr;
        return nodePtr;
    }

    public List_t(int chk) {
        this.head = new List_Node();
        this.chk = chk;
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
        return (nodePtr.dataPtr);
    }

    public int compare(Object obj1, Object obj2) {
        if (chk == 1) {
            return Packet.compareFragmentID((Packet) obj1, (Packet) obj2);
        } else return compareObject(obj1, obj2);
    }

    public boolean insert(Object dataPtr) {
        List_Node prevPtr;
        List_Node nodePtr;
        List_Node currPtr;
        prevPtr = findPrevious(dataPtr);
        currPtr = prevPtr.nextPtr;
        nodePtr = allocNode(dataPtr);
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
            nodePtr = null;
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
}
