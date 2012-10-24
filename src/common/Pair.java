package common;

import java.util.*;

public class Pair {

    public Object first;

    public Object second;

    public Pair() {
        first = null;
        second = null;
    }

    public static Pair alloc(Object first, Object second) {
        Pair ptr = new Pair();
        ptr.first = first;
        ptr.second = second;
        return ptr;
    }

    public static Pair Ppair_alloc(Object firstPtr, Object secondPtr) {
        Pair pairPtr = new Pair();
        pairPtr.first = firstPtr;
        pairPtr.second = secondPtr;
        return pairPtr;
    }

    public static void free(Pair pairPtr) {
        pairPtr = null;
    }

    public static void swap(Pair pairPtr) {
        Object tmpPtr = pairPtr.first;
        pairPtr.first = pairPtr.second;
        pairPtr.second = tmpPtr;
    }
}
