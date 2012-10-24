package Labyrinth3D;

public class List_Iter {

    List_Node itPtr;

    public List_Iter() {
        itPtr = null;
    }

    public void reset(List_t listPtr) {
        itPtr = listPtr.head;
    }

    public boolean hasNext(List_t listPtr) {
        return (itPtr.nextPtr != null) ? true : false;
    }

    public Object next(List_t listPtr) {
        itPtr = itPtr.nextPtr;
        return itPtr.dataPtr;
    }
}
