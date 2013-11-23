package Yada.java;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Barrier {

    public static CyclicBarrier cyclicBarrier = null;

    public static void enterBarrier() {
        if (cyclicBarrier == null) return;
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public static void setBarrier(int x) {
        cyclicBarrier = new CyclicBarrier(x, null);
    }
}
