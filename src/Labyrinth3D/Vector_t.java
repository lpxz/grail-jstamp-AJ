package Labyrinth3D;

public class Vector_t {

    int size;

    int capacity;

    Object[] elements;

    public Vector_t() {
    }

    public static Vector_t vector_alloc(int initCapacity) {
        int capacity = Math.max(initCapacity, 1);
        Vector_t vectorPtr = new Vector_t();
        vectorPtr.capacity = capacity;
        vectorPtr.elements = new Object[capacity];
        return vectorPtr;
    }

    public void vector_free() {
        elements = null;
    }

    public Object vector_at(int i) {
        return (elements[i]);
    }

    public boolean vector_pushBack(Object dataPtr) {
        if (size == capacity) {
            int newCapacity = capacity * 2;
            Object[] newElements = new Object[newCapacity];
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

    public static boolean vector_copy(Vector_t dstVectorPtr, Vector_t srcVectorPtr) {
        int dstCapacity = dstVectorPtr.capacity;
        int srcSize = srcVectorPtr.size;
        if (dstCapacity < srcSize) {
            int srcCapacity = srcVectorPtr.capacity;
            Object[] elements = new Object[srcCapacity];
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
