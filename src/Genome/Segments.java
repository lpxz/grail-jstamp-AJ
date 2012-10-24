package Genome;

import java.util.Vector;
import common.BitMap;
import common.Random;

public class Segments {

    public int length;

    public int minNum;

    Vector contentsPtr;

    ByteString strings[];

    Segments(int myLength, int myMinNum) {
        minNum = myMinNum;
        length = myLength;
        strings = new ByteString[minNum];
        contentsPtr = new Vector(minNum);
    }

    void create(Gene genePtr, Random randomPtr) {
        ByteString geneString;
        int geneLength;
        BitMap startBitmapPtr;
        int numStart;
        int i;
        int maxZeroRunLength;
        geneString = genePtr.contents;
        geneLength = genePtr.length;
        startBitmapPtr = genePtr.startBitmapPtr;
        numStart = geneLength - length + 1;
        for (i = 0; i < minNum; i++) {
            int j = (int) (randomPtr.random_generate() % numStart);
            boolean status = startBitmapPtr.bitmap_set(j);
            strings[i] = geneString.substring(j, j + length);
            contentsPtr.addElement(strings[i]);
        }
        i = 0;
        if (!startBitmapPtr.bitmap_isSet(i)) {
            ByteString string = geneString.subString(i, i + length);
            contentsPtr.addElement(string);
            startBitmapPtr.bitmap_set(i);
        }
        maxZeroRunLength = length - 1;
        for (i = 0; i < numStart; i++) {
            int i_stop = Math.min((i + maxZeroRunLength), numStart);
            for (; i < i_stop; i++) {
                if (startBitmapPtr.bitmap_isSet(i)) {
                    break;
                }
            }
            if (i == i_stop) {
                i = i - 1;
                ByteString string = geneString.subString(i, i + length);
                contentsPtr.addElement(string);
                startBitmapPtr.bitmap_set(i);
            }
        }
    }
}
