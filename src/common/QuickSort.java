package common;

import javax.swing.plaf.basic.BasicTreeUI.TreeCancelEditingAction;

public class QuickSort {

    public QuickSort() {
    }

    /**
   * Sort an array of Objects into ascending order. The sort algorithm is an optimised
   * quicksort, as described in Jon L. Bentley and M. Douglas McIlroy's
   * "Engineering a Sort Function", Software-Practice and Experience, Vol.
   * 23(11) P. 1249-1265 (November 1993). This algorithm gives nlog(n)
   * performance on many arrays that would take quadratic time with a standard
   * quicksort.
   *
   * @param a the array to sort
   */
    public void sort(Object[] a) {
        qsort(a, 0, a.length);
    }

    public void sort(Object[] a, int fromIndex, int toIndex) {
        qsort(a, fromIndex, toIndex);
    }

    private int med3(int a, int b, int c, Object[] d) {
        if (less(d[a], d[b])) {
            if (less(d[b], d[c])) {
                return b;
            } else {
                if (less(d[a], d[c])) return c; else return a;
            }
        } else {
            if (less(d[c], d[b])) {
                return b;
            } else {
                if (less(d[c], d[a])) return c; else return a;
            }
        }
    }

    private void swap(int i, int j, Object[] a) {
        Object c = a[i];
        a[i] = a[j];
        a[j] = c;
    }

    private void qsort(Object[] a, int start, int n) {
        if (n <= 7) {
            for (int i = start + 1; i < start + n; i++) for (int j = i; j > 0 && less(a[j], a[j - 1]); j--) swap(j, j - 1, a);
            return;
        }
        int pm = n / 2;
        if (n > 7) {
            int pl = start;
            int pn = start + n - 1;
            if (n > 40) {
                int s = n / 8;
                pl = med3(pl, pl + s, pl + 2 * s, a);
                pm = med3(pm - s, pm, pm + s, a);
                pn = med3(pn - 2 * s, pn - s, pn, a);
            }
            pm = med3(pl, pm, pn, a);
        }
        int pa, pb, pc, pd, pv;
        int r;
        pv = start;
        swap(pv, pm, a);
        pa = pb = start;
        pc = pd = start + n - 1;
        while (true) {
            while (pb <= pc && (r = diff(a[pb], a[pv])) <= 0) {
                if (r == 0) {
                    swap(pa, pb, a);
                    pa++;
                }
                pb++;
            }
            while (pc >= pb && (r = diff(a[pc], a[pv])) >= 0) {
                if (r == 0) {
                    swap(pc, pd, a);
                    pd--;
                }
                pc--;
            }
            if (pb > pc) break;
            swap(pb, pc, a);
            pb++;
            pc--;
        }
        int pn = start + n;
        int s;
        s = Math.min(pa - start, pb - pa);
        vecswap(start, pb - s, s, a);
        s = Math.min(pd - pc, pn - pd - 1);
        vecswap(pb, pn - s, s, a);
        if ((s = pb - pa) > 1) qsort(a, start, s);
        if ((s = pd - pc) > 1) qsort(a, pn - s, s);
    }

    private void vecswap(int i, int j, int n, Object[] a) {
        for (; n > 0; i++, j++, n--) swap(i, j, a);
    }

    public boolean less(Object x, Object y) {
        if (x instanceof Comparable && y instanceof Comparable) {
            Comparable xcomp = (Comparable) x;
            int result = xcomp.compareTo((Comparable) y);
            if (result < 0) return true; else {
                return false;
            }
        } else {
            throw new RuntimeException("impossible to cmopare");
        }
    }

    public int diff(Object x, Object y) {
        if (x instanceof Integer && y instanceof Integer) {
            Integer xcomp = (Integer) x;
            Integer ycomp = (Integer) y;
            return xcomp - ycomp;
        } else {
            throw new RuntimeException("impossible to cmopare");
        }
    }
}
