package Vacation;

public class List_t {

    public List_Node head;

    int size;

    public List_t() {
        head = new List_Node();
    }

    private List_Node allocNode(Object dataPtr) {
        List_Node nodePtr = new List_Node();
        if (nodePtr == null) {
            return null;
        }
        nodePtr.dataPtr = dataPtr;
        nodePtr.nextPtr = null;
        return nodePtr;
    }

    public static List_t alloc() {
        List_t listPtr = new List_t();
        if (listPtr == null) {
            return null;
        }
        listPtr.head.dataPtr = null;
        listPtr.head.nextPtr = null;
        listPtr.size = 0;
        return listPtr;
    }

    public static void free(List_t listPtr) {
        listPtr = null;
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
        Reservation_Info aPtr = (Reservation_Info) obj1;
        Reservation_Info bPtr = (Reservation_Info) obj2;
        int typeDiff;
        typeDiff = aPtr.type - bPtr.type;
        return ((typeDiff != 0) ? (typeDiff) : (aPtr.id - bPtr.id));
    }

    public boolean insert(Object dataPtr) {
        List_Node prevPtr;
        List_Node nodePtr;
        List_Node currPtr;
        prevPtr = findPrevious(dataPtr);
        currPtr = prevPtr.nextPtr;
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

    public static void main(String[] argv) {
        List_t listPtr;
        int[] data1 = new int[5];
        int[] data2 = new int[6];
        int i;
        System.out.println("Starting...");
    }
}
