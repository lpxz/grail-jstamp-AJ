package Genome;

public class ListNode {
    /*atomicset(N)*/
   /*atoimc(N)*/ Pair dataPtr /*P=this.N*/;

  /*atomic(N)*/  ListNode nextPtr /*N=this.N*/;

    public ListNode() {
        dataPtr = null;
        nextPtr = null;
    }

    public ListNode(Pair myDataPtr) {
        dataPtr = myDataPtr;
        nextPtr = null;
    }
}
