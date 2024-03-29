package Bayes;

public class Queue {

    private static int QUEUE_GROWTH_FACTOR = 2;

    int pop;

    int push;

    int capacity;

    int[] elements;

    public Queue() {
    }

    public static Queue queue_alloc(int initCapacity) {
        Queue queuePtr = new Queue();
        int capacity = ((initCapacity < 2) ? 2 : initCapacity);
        queuePtr.elements = new int[capacity];
        queuePtr.pop = capacity - 1;
        queuePtr.push = 0;
        queuePtr.capacity = capacity;
        return queuePtr;
    }

    public Queue Pqueue_alloc(int initCapacity) {
        Queue queuePtr = new Queue();
        int capacity = ((initCapacity < 2) ? 2 : initCapacity);
        queuePtr.elements = new int[capacity];
        queuePtr.pop = capacity - 1;
        queuePtr.push = 0;
        queuePtr.capacity = capacity;
        return queuePtr;
    }

    public Queue TMqueue_alloc(int initCapacity) {
        Queue queuePtr = new Queue();
        int capacity = ((initCapacity < 2) ? 2 : initCapacity);
        queuePtr.elements = new int[capacity];
        queuePtr.pop = capacity - 1;
        queuePtr.push = 0;
        queuePtr.capacity = capacity;
        return queuePtr;
    }

    public void queue_free() {
        elements = null;
    }

    public void Pqueue_free() {
        elements = null;
    }

    public void TMqueue_free() {
        elements = null;
    }

    public boolean queue_isEmpty() {
        return (((pop + 1) % capacity == push) ? true : false);
    }

    public void queue_clear() {
        pop = capacity - 1;
        push = 0;
    }

    public boolean TMqueue_isEmpty(Queue queuePtr) {
        int pop = queuePtr.pop;
        int push = queuePtr.push;
        int capacity = queuePtr.capacity;
        return (((pop + 1) % capacity == push) ? true : false);
    }

    public void queue_shuffle(Queue queuePtr, Random randomPtr) {
        int pop = queuePtr.pop;
        int push = queuePtr.push;
        int capacity = queuePtr.capacity;
        int numElement;
        if (pop < push) {
            numElement = push - (pop + 1);
        } else {
            numElement = capacity - (pop - push + 1);
        }
        int[] elements = queuePtr.elements;
        int i;
        int base = pop + 1;
        for (i = 0; i < numElement; i++) {
            int r1 = (int) (randomPtr.random_generate() % numElement);
            int r2 = (int) (randomPtr.random_generate() % numElement);
            int i1 = (base + r1) % capacity;
            int i2 = (base + r2) % capacity;
            int tmp = elements[i1];
            elements[i1] = elements[i2];
            elements[i2] = tmp;
        }
    }

    public boolean queue_push(int dataPtr) {
        int newPush = (push + 1) % capacity;
        if (newPush == pop) {
            int newCapacity = capacity * QUEUE_GROWTH_FACTOR;
            int[] newElements = new int[newCapacity];
            int dst = 0;
            int[] tmpelements = elements;
            if (pop < push) {
                int src;
                for (src = (pop + 1); src < push; src++, dst++) {
                    newElements[dst] = elements[src];
                }
            } else {
                int src;
                for (src = (pop + 1); src < capacity; src++, dst++) {
                    newElements[dst] = elements[src];
                }
                for (src = 0; src < push; src++, dst++) {
                    newElements[dst] = elements[src];
                }
            }
            elements = newElements;
            pop = newCapacity - 1;
            capacity = newCapacity;
            push = dst;
            newPush = push + 1;
        }
        elements[push] = dataPtr;
        push = newPush;
        return true;
    }

    public boolean Pqueue_push(Queue queuePtr, int dataPtr) {
        int pop = queuePtr.pop;
        int push = queuePtr.push;
        int capacity = queuePtr.capacity;
        int newPush = (push + 1) % capacity;
        if (newPush == pop) {
            int newCapacity = capacity * QUEUE_GROWTH_FACTOR;
            int[] newElements = new int[newCapacity];
            int dst = 0;
            int[] elements = queuePtr.elements;
            if (pop < push) {
                int src;
                for (src = (pop + 1); src < push; src++, dst++) {
                    newElements[dst] = elements[src];
                }
            } else {
                int src;
                for (src = (pop + 1); src < capacity; src++, dst++) {
                    newElements[dst] = elements[src];
                }
                for (src = 0; src < push; src++, dst++) {
                    newElements[dst] = elements[src];
                }
            }
            elements = null;
            queuePtr.elements = newElements;
            queuePtr.pop = newCapacity - 1;
            queuePtr.capacity = newCapacity;
            push = dst;
            newPush = push + 1;
        }
        queuePtr.elements[push] = dataPtr;
        queuePtr.push = newPush;
        return true;
    }

    public boolean TMqueue_push(Queue queuePtr, int dataPtr) {
        int pop = (queuePtr.pop);
        int push = (queuePtr.push);
        int capacity = (queuePtr.capacity);
        int newPush = (push + 1) % capacity;
        if (newPush == pop) {
            int newCapacity = capacity * QUEUE_GROWTH_FACTOR;
            int[] newElements = new int[newCapacity];
            int dst = 0;
            int[] elements = queuePtr.elements;
            if (pop < push) {
                int src;
                for (src = (pop + 1); src < push; src++, dst++) {
                    newElements[dst] = (elements[src]);
                }
            } else {
                int src;
                for (src = (pop + 1); src < capacity; src++, dst++) {
                    newElements[dst] = (elements[src]);
                }
                for (src = 0; src < push; src++, dst++) {
                    newElements[dst] = (elements[src]);
                }
            }
            elements = null;
            queuePtr.elements = newElements;
            queuePtr.pop = newCapacity - 1;
            queuePtr.capacity = newCapacity;
            push = dst;
            newPush = push + 1;
        }
        int[] elements = queuePtr.elements;
        elements[push] = dataPtr;
        queuePtr.push = newPush;
        return true;
    }

    public int queue_pop() {
        int newPop = (pop + 1) % capacity;
        if (newPop == push) {
            return 0;
        }
        int dataPtr = elements[newPop];
        pop = newPop;
        return dataPtr;
    }

    public int TMqueue_pop(Queue queuePtr) {
        int pop = queuePtr.pop;
        int push = queuePtr.push;
        int capacity = queuePtr.capacity;
        int newPop = (pop + 1) % capacity;
        if (newPop == push) {
            return 0;
        }
        int[] elements = queuePtr.elements;
        int dataPtr = elements[newPop];
        queuePtr.pop = newPop;
        return dataPtr;
    }
}
