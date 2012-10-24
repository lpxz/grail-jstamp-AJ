package Intruder;

public class List_Iter {

    List_Node itPtr;

    public List_Iter() {
    }

    public void reset(List_t listPtr) {
        itPtr = listPtr.head;
    }

    public boolean hasNext(List_t listPtr) {
        return itPtr.nextPtr != null;
    }

    public Object next(List_t listPtr) {
        itPtr = itPtr.nextPtr;
        return itPtr.dataPtr;
    }
}
