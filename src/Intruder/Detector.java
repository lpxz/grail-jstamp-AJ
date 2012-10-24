package Intruder;

public class Detector {

    Dictionary dictionaryPtr;

    Vector_t preprocessorVectorPtr;

    public Detector() {
        dictionaryPtr = new Dictionary();
        preprocessorVectorPtr = new Vector_t(1);
    }

    public void addPreprocessor(int p) {
        boolean status = preprocessorVectorPtr.vector_pushBack(new Integer(p));
    }

    public int process(byte[] str) {
        int numPreprocessor = preprocessorVectorPtr.vector_getSize();
        for (int p = 0; p < numPreprocessor; p++) {
            Integer preprocessor = (Integer) preprocessorVectorPtr.vector_at(p);
            if (preprocessor.intValue() == 1) {
            } else if (preprocessor.intValue() == 2) {
                for (int i = 0; i < str.length; i++) {
                    if (str[i] > 'A' && str[i] < 'Z') {
                        str[i] += (byte) 32;
                    }
                }
            }
        }
        ERROR err = new ERROR();
        String signature = dictionaryPtr.match(new String(str));
        if (signature != null) {
            return err.SIGNATURE;
        }
        return err.NONE;
    }
}
