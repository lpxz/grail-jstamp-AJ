package common;

public class BitMap {

    private static int NUM_BIT_PER_BYTE = (8);

    private static int NUM_BIT_PER_WORD = (4 * NUM_BIT_PER_BYTE);

    int numBit;

    int numWord;

    int[] bits;

    public BitMap() {
    }

    public int DIVIDE_AND_ROUND_UP(int a, int b) {
        int res1 = a / b;
        int res2 = a % b;
        int val = (res2 > 0) ? (1) : (0);
        return (res1 + val);
    }

    public static BitMap bitmap_alloc(int numBit) {
        BitMap bitmapPtr = new BitMap();
        bitmapPtr.numBit = numBit;
        int numWord = bitmapPtr.DIVIDE_AND_ROUND_UP(numBit, NUM_BIT_PER_WORD);
        bitmapPtr.numWord = numWord;
        bitmapPtr.bits = new int[numWord];
        for (int i = 0; i < numWord; i++) bitmapPtr.bits[i] = 0;
        return bitmapPtr;
    }

    public void bitmap_free() {
        bits = null;
    }

    public boolean bitmap_set(int i) {
        if ((i < 0) || (i >= numBit)) {
            return false;
        }
        bits[i / NUM_BIT_PER_WORD] |= (1 << (i % NUM_BIT_PER_WORD));
        return true;
    }

    public void bitmap_clearAll() {
        for (int i = 0; i < numWord; i++) bits[i] = 0;
    }

    public boolean bitmap_isSet(int i) {
        int val = bits[i / NUM_BIT_PER_WORD] & (1 << (i % NUM_BIT_PER_WORD));
        if ((i >= 0) && (i < numBit) && (val != 0)) {
            return true;
        }
        return false;
    }

    public int bitmap_findClear(int startIndex) {
        int tmp_numBit = numBit;
        for (int i = Math.max(startIndex, 0); i < tmp_numBit; i++) {
            int val = bits[i / NUM_BIT_PER_WORD] & (1 << (i % NUM_BIT_PER_WORD));
            if (val == 0) {
                return i;
            }
        }
        return -1;
    }
}
