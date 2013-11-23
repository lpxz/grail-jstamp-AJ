public class Test {

    public static void main(String[] args) {
        Library lib = new Library();
        synchronized ("haha") {
            boolean shouldCorrectify = false;
            lib.update(50);
            lib.greaterThan100();
        }
    }
}
