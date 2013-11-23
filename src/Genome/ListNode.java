package Genome;

public class ListNode {

    Pair dataPtr;

    ListNode nextPtr;

    public ListNode() {
        dataPtr = null;
        nextPtr = null;
    }

    public ListNode(Pair myDataPtr) {
        dataPtr = myDataPtr;
        nextPtr = null;
    }
}
