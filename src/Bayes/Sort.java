package Bayes;

public class Sort {

    private static int CUTOFF = 8;

    int[] lostk;

    int[] histk;

    public Sort() {
        lostk = new int[30];
        histk = new int[30];
    }

    public static void swap(byte[] base, int a, int b, int width) {
        if (a != b) {
            while (width-- != 0) {
                byte tmp = base[a];
                base[a++] = base[b];
                base[b++] = tmp;
            }
        }
    }

    public static void shortsort(byte[] base, int lo, int hi, int width, int n, int offset) {
        while (hi > lo) {
            int max = lo;
            for (int p = (lo + width); p <= hi; p += width) {
                if (cmp(base, p, max, n, offset) > 0) {
                    max = p;
                }
            }
            swap(base, max, hi, width);
            hi -= width;
        }
    }

    public void sort(byte[] base, int start, int num, int width, int n, int offset) {
        if (num < 2 || width == 0) {
            return;
        }
        int[] lostk = this.lostk;
        int[] histk = this.histk;
        int stkptr = 0;
        int lo = start;
        int hi = start + (width * (num - 1));
        int size = 0;
        int pvlo = lo;
        int pvhi = hi;
        int pvwidth = width;
        int pvn = n;
        int pvmid;
        int pvloguy;
        int pvhiguy;
        int typeflag;
        while (true) {
            size = (pvhi - pvlo) / pvwidth + 1;
            if (size <= CUTOFF) {
                shortsort(base, pvlo, pvhi, pvwidth, pvn, offset);
            } else {
                pvmid = pvlo + (size / 2) * pvwidth;
                swap(base, pvmid, pvlo, pvwidth);
                pvloguy = pvlo;
                pvhiguy = pvhi + pvwidth;
                while (true) {
                    do {
                        pvloguy += pvwidth;
                    } while (pvloguy <= pvhi && cmp(base, pvloguy, pvlo, pvn, offset) <= 0);
                    do {
                        pvhiguy -= pvwidth;
                    } while (pvhiguy > pvlo && cmp(base, pvhiguy, pvlo, pvn, offset) >= 0);
                    if (pvhiguy < pvloguy) {
                        break;
                    }
                    swap(base, pvloguy, pvhiguy, pvwidth);
                }
                swap(base, pvlo, pvhiguy, pvwidth);
                if ((pvhiguy - 1 - pvlo) >= (pvhi - pvloguy)) {
                    if (pvlo + pvwidth < pvhiguy) {
                        lostk[stkptr] = pvlo;
                        histk[stkptr] = pvhiguy - pvwidth;
                        ++stkptr;
                    }
                    if (pvloguy < pvhi) {
                        pvlo = pvloguy;
                        continue;
                    }
                } else {
                    if (pvloguy < pvhi) {
                        lostk[stkptr] = pvloguy;
                        histk[stkptr] = pvhi;
                        ++stkptr;
                    }
                    if (pvlo + pvwidth < pvhiguy) {
                        pvhi = pvhiguy - pvwidth;
                        continue;
                    }
                }
            }
            --stkptr;
            if (stkptr >= 0) {
                pvlo = lostk[stkptr];
                pvhi = histk[stkptr];
                continue;
            }
            break;
        }
    }

    public static int cmp(byte[] base, int p1, int p2, int n, int offset) {
        int i = n - offset;
        int s1 = p1 + offset;
        int s2 = p2 + offset;
        while (i-- > 0) {
            byte u1 = base[s1];
            byte u2 = base[s2];
            if (u1 != u2) {
                return (u1 - u2);
            }
            s1++;
            s2++;
        }
        return 0;
    }
}
