package Yada.java;

import java.util.*;

public class edge {

    public Object firstPtr;

    public Object secondPtr;

    public edge() {
        firstPtr = null;
        secondPtr = null;
    }

    public edge(Object first, Object second) {
        this.firstPtr = first;
        this.secondPtr = second;
    }

    public void swap() {
        Object tmpPtr = firstPtr;
        firstPtr = secondPtr;
        secondPtr = tmpPtr;
    }
}
