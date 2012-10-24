package Labyrinth3D;

public class List_Node {
    /*atomicset(N)*/
    Object dataPtr;

   /*atomic(N)*/ List_Node nextPtr /*N=this.N*/;

    public List_Node() {
        dataPtr = null;
        nextPtr = null;
    }
}
