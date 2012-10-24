package Labyrinth3D;

public class Queue_t {
    /*atomicset(Q)*/
    public static int QUEUE_GROWTH_FACTOR = 2;

   /*atomic(Q)*/ int pop;

  /*atomic(Q)*/  int push;

    int capacity;

    /*atomic(Q)*/Object[] elements /*this.Q[]=this.Q*/;

    public Queue_t() {
    }

    public static Queue_t queue_alloc(int initCapacity) {
        Queue_t queuePtr = new Queue_t();
        int capacity = ((initCapacity < 2) ? 2 : initCapacity);
        queuePtr.elements = new Object[capacity];
        queuePtr.pop = capacity - 1;
        queuePtr.push = 0;
        queuePtr.capacity = capacity;
        return queuePtr;
    }

    public Queue_t Pqueue_alloc(int initCapacity) {
        Queue_t queuePtr = new Queue_t();
        int capacity = ((initCapacity < 2) ? 2 : initCapacity);
        queuePtr.elements = new Object[capacity];
        queuePtr.pop = capacity - 1;
        queuePtr.push = 0;
        queuePtr.capacity = capacity;
        return queuePtr;
    }

    public void queue_free(Queue_t queuePtr) {
        queuePtr.elements = null;
        queuePtr = null;
    }

    public void Pqueue_free(Queue_t queuePtr) {
        queuePtr.elements = null;
        queuePtr = null;
    }

    public boolean queue_isEmpty() {
        return (((pop + 1) % capacity == push) ? true : false);
    }

    public void queue_clear() {
        pop = capacity - 1;
        push = 0;
    }

    public boolean queue_push(Object dataPtr) {
        int newPush = (push + 1) % capacity;
        if (newPush == pop) {
            int newCapacity = capacity * QUEUE_GROWTH_FACTOR;
            Object[] newElements = new Object[newCapacity];
            int dst = 0;
            Object[] tmpelements = elements;
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

    public boolean Pqueue_push(Queue_t queuePtr, Object dataPtr) {
        int pop = queuePtr.pop;
        int push = queuePtr.push;
        int capacity = queuePtr.capacity;
        int newPush = (push + 1) % capacity;
        if (newPush == pop) {
            int newCapacity = capacity * QUEUE_GROWTH_FACTOR;
            Object[] newElements = new Object[newCapacity];
            int dst = 0;
            Object[] elements = queuePtr.elements;
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

    public Object queue_pop() {
        int newPop = (pop + 1) % capacity;
        if (newPop == push) {
            return null;
        }
        Object dataPtr = elements[newPop];
        pop = newPop;
        return dataPtr;
    }
}
