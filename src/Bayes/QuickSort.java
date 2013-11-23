package Bayes;

public class QuickSort {

    public QuickSort() {
    }

    public static void sort(Object[] a) {
        qsort(a, 0, a.length);
    }

    public static void sort(Object[] a, int fromIndex, int toIndex) {
        qsort(a, fromIndex, toIndex);
    }

    private static int med3(int a, int b, int c, Object[] d) {
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

    private static void swap(int i, int j, Object[] a) {
        Object c = a[i];
        a[i] = a[j];
        a[j] = c;
    }

    private static void qsort(Object[] a, int start, int n) {
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

    private static void vecswap(int i, int j, int n, Object[] a) {
        for (; n > 0; i++, j++, n--) swap(i, j, a);
    }

    public static boolean less(Object x, Object y) {
        Query aQueryPtr = (Query) x;
        Query bQueryPtr = (Query) y;
        if (aQueryPtr.index < bQueryPtr.index) return true;
        return false;
    }

    public static int diff(Object x, Object y) {
        Query aQueryPtr = (Query) x;
        Query bQueryPtr = (Query) y;
        return (aQueryPtr.index - bQueryPtr.index);
    }
}
