package Genome;

import common.BitMap;
import common.Random;

public class Gene {

    public int length;

    public ByteString contents;

    public BitMap startBitmapPtr;

    Gene(int myLength) {
        length = myLength;
        startBitmapPtr = BitMap.bitmap_alloc(length);
    }

    void create(Random randomObj) {
        int i;
        byte[] nucleotides = new byte[4];
        byte[] arrayContents = new byte[length];
        nucleotides[0] = (byte) 'a';
        nucleotides[1] = (byte) 'c';
        nucleotides[2] = (byte) 'g';
        nucleotides[3] = (byte) 't';
        for (i = 0; i < length; i++) {
            arrayContents[i] = nucleotides[(int) (randomObj.random_generate() % 4)];
        }
        contents = new ByteString(arrayContents);
    }
}
