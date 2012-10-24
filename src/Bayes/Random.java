package Bayes;

public class Random {

    long[] mt;

    int mti;

    int RANDOM_DEFAULT_SEED;

    public Random() {
        RANDOM_DEFAULT_SEED = 0;
        mt = new long[624];
    }

    public void random_alloc() {
        init_genrand(this.RANDOM_DEFAULT_SEED);
    }

    public void init_genrand(int s) {
        mt[0] = ((long) s) & 0xFFFFFFFFL;
        for (int mti = 1; mti < 624; mti++) {
            mt[mti] = (1812433253L * (mt[mti - 1] ^ (mt[mti - 1] >> 30)) + ((long) mti));
            mt[mti] &= 0xFFFFFFFFL;
        }
        this.mti = 624;
    }

    public void random_seed(int seed) {
        init_genrand(seed);
    }

    public long random_generate() {
        long x = genrand_int32() & 0xFFFFFFFFL;
        return x;
    }

    public long posrandom_generate() {
        long r = genrand_int32();
        if (r > 0) return r; else return -r;
    }

    public long genrand_int32() {
        long y;
        int mti = this.mti;
        long[] mt = this.mt;
        if (mti >= 624) {
            int kk;
            if (mti == 624 + 1) {
                init_genrand(5489);
                mti = this.mti;
            }
            for (kk = 0; kk < (624 - 397); kk++) {
                y = (mt[kk] & 0x80000000L) | (mt[kk + 1] & 0x7fffffffL);
                mt[kk] = mt[kk + 397] ^ (y >> 1) ^ ((y & 0x1) == 0 ? 0L : 0x9908b0dfL);
            }
            for (; kk < (624 - 1); kk++) {
                y = (mt[kk] & 0x80000000L) | (mt[kk + 1] & 0x7fffffffL);
                mt[kk] = mt[kk + (397 - 624)] ^ (y >> 1) ^ ((y & 0x1) == 0 ? 0L : 0x9908b0dfL);
            }
            y = (mt[624 - 1] & 0x80000000L) | (mt[0] & 0x7fffffffL);
            mt[624 - 1] = mt[397 - 1] ^ (y >> 1) ^ ((y & 0x1) == 0 ? 0L : 0x9908b0dfL);
            mti = 0;
        }
        y = mt[mti++];
        y ^= (y >> 11);
        y ^= (y << 7) & 0x9d2c5680L;
        y ^= (y << 15) & 0xefc60000L;
        y ^= (y >> 18);
        this.mti = mti;
        return y;
    }
}
