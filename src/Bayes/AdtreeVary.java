package Bayes;

public class AdtreeVary {

    int index;

    int mostCommonValue;

    AdtreeNode zeroNodePtr;

    AdtreeNode oneNodePtr;

    public AdtreeVary() {
    }

    public AdtreeVary(int index) {
        this.index = index;
        mostCommonValue = -1;
    }

    public void free_vary() {
        zeroNodePtr = null;
        oneNodePtr = null;
    }
}
