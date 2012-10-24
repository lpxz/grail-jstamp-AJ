package common;

public class Vector_t {

    int size;

    int capacity;

    Object[] elements;

    QuickSort qsort;

    public Vector_t() {
        qsort = new QuickSort();
    }

    public static Vector_t vector_alloc(int initCapacity) {
        int capacity = Math.max(initCapacity, 1);
        Vector_t vectorPtr = new Vector_t();
        if (vectorPtr != null) {
            vectorPtr.size = 0;
            vectorPtr.capacity = capacity;
            vectorPtr.elements = new Object[capacity];
            if (vectorPtr.elements == null) return null;
        }
        return vectorPtr;
    }

    public void vector_free() {
        elements = null;
    }

    public Object vector_at(int i) {
        if ((i < 0) || (i >= size)) {
            System.out.println("Illegal Vector.element\n");
            return null;
        }
        return (elements[i]);
    }

    public boolean vector_pushBack(Object dataPtr) {
        if (size == capacity) {
            int newCapacity = capacity * 2;
            Object[] newElements = new Object[newCapacity];
            if (newElements == null) {
                return false;
            }
            capacity = newCapacity;
            for (int i = 0; i < size; i++) {
                newElements[i] = elements[i];
            }
            elements = null;
            elements = newElements;
        }
        elements[size++] = dataPtr;
        return true;
    }

    public Object vector_popBack() {
        if (size < 1) {
            return null;
        }
        return (elements[--(size)]);
    }

    public int vector_getSize() {
        return (size);
    }

    public void vector_clear() {
        size = 0;
    }

    public void vector_sort() {
        qsort.sort(elements, 0, size);
    }

    public static boolean vector_copy(Vector_t dstVectorPtr, Vector_t srcVectorPtr) {
        int dstCapacity = dstVectorPtr.capacity;
        int srcSize = srcVectorPtr.size;
        if (dstCapacity < srcSize) {
            int srcCapacity = srcVectorPtr.capacity;
            Object[] elements = new Object[srcCapacity];
            if (elements == null) {
                return false;
            }
            dstVectorPtr.elements = null;
            dstVectorPtr.elements = elements;
            dstVectorPtr.capacity = srcCapacity;
        }
        for (int i = 0; i < srcSize; i++) {
            dstVectorPtr.elements[i] = srcVectorPtr.elements[i];
        }
        dstVectorPtr.size = srcSize;
        return true;
    }
}
