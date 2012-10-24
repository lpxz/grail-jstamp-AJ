package Genome;

public class Pair {
    /*atomicset(P)*/
   /*atomic(P)*/ ByteString firstPtr ;

    ByteString secondPtr;

    public Pair() {
        firstPtr = null;
        secondPtr = null;
    }

    public Pair(ByteString myFirstPtr, ByteString mySecondPtr) {
        firstPtr = myFirstPtr;
        secondPtr = mySecondPtr;
    }
}
