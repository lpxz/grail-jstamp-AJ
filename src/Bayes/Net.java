package Bayes;

import Genome.constructEntry;
import common.BitMap;

public class Net {

    private static int NET_NODE_MARK_INIT = 0;

    private static int NET_NODE_MARK_DONE = 1;

    private static int NET_NODE_MARK_TEST = 2;

    private static int OPERATION_INSERT = 0;

    private static int OPERATION_REMOVE = 1;

    private static int OPERATION_REVERSE = 2;

    NetNode nn;

    Vector_t nodeVectorPtr;

    public Net() {
    }

    public static NetNode allocNode(int id) {
        NetNode nodePtr = new NetNode();
        nodePtr.parentIdListPtr = IntList.list_alloc();
        nodePtr.childIdListPtr = IntList.list_alloc();
        nodePtr.id = id;
        return nodePtr;
    }

    public Net(int numNode) {
        Vector_t nodeVectorPtr = new Vector_t(numNode);
        for (int i = 0; i < numNode; i++) {
            NetNode nodePtr = allocNode(i);
            boolean status = nodeVectorPtr.vector_pushBack(nodePtr);
        }
        this.nodeVectorPtr = nodeVectorPtr;
    }

    public void net_free() {
        nn = null;
        nodeVectorPtr = null;
    }

    public void insertEdge(int fromId, int toId) {
        boolean status;
        NetNode childNodePtr = (NetNode) (nodeVectorPtr.vector_at(toId));
        IntList parentIdListPtr = childNodePtr.parentIdListPtr;
        status = parentIdListPtr.list_insert(fromId);
        NetNode parentNodePtr = (NetNode) (nodeVectorPtr.vector_at(fromId));
        IntList childIdListPtr = parentNodePtr.childIdListPtr;
        status = childIdListPtr.list_insert(toId);
    }

    public void removeEdge(int fromId, int toId) {
        boolean status;
        NetNode childNodePtr = (NetNode) (nodeVectorPtr.vector_at(toId));
        IntList parentIdListPtr = childNodePtr.parentIdListPtr;
        status = parentIdListPtr.list_remove(fromId);
        NetNode parentNodePtr = (NetNode) (nodeVectorPtr.vector_at(fromId));
        IntList childIdListPtr = parentNodePtr.childIdListPtr;
        status = childIdListPtr.list_remove(toId);
    }

    public void reverseEdge(int fromId, int toId) {
        removeEdge(fromId, toId);
        insertEdge(toId, fromId);
    }

    public void net_applyOperation(int op, int fromId, int toId) {
        if (op == OPERATION_INSERT) {
            insertEdge(fromId, toId);
        } else if (op == OPERATION_REMOVE) {
            removeEdge(fromId, toId);
        } else if (op == OPERATION_REVERSE) {
            reverseEdge(fromId, toId);
        }
    }

    public boolean net_hasEdge(int fromId, int toId) {
        NetNode childNodePtr = (NetNode) (nodeVectorPtr.vector_at(toId));
        IntList parentIdListPtr = childNodePtr.parentIdListPtr;
        IntListNode it = parentIdListPtr.head;
        while (it.nextPtr != null) {
            it = it.nextPtr;
            int parentId = it.dataPtr;
            if (parentId == fromId) {
                return true;
            }
        }
        return false;
    }

    public boolean TMnet_hasEdge(int fromId, int toId) {
        NetNode childNodePtr = (NetNode) (nodeVectorPtr.vector_at(toId));
        IntList parentIdListPtr = childNodePtr.parentIdListPtr;
        IntListNode it = parentIdListPtr.head;
        while (it.nextPtr != null) {
            it = it.nextPtr;
            int parentId = it.dataPtr;
            if (parentId == fromId) {
                return true;
            }
        }
        return false;
    }

    public boolean net_isPath(int fromId, int toId, BitMap visitedBitmapPtr, Queue workQueuePtr) {
        boolean status;
        visitedBitmapPtr.bitmap_clearAll();
        workQueuePtr.queue_clear();
        status = workQueuePtr.queue_push(fromId);
        while (!workQueuePtr.queue_isEmpty()) {
            int id = workQueuePtr.queue_pop();
            if (id == toId) {
                workQueuePtr.queue_clear();
                return true;
            }
            status = visitedBitmapPtr.bitmap_set(id);
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(id));
            IntList childIdListPtr = nodePtr.childIdListPtr;
            IntListNode it = childIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int childId = it.dataPtr;
                if (!visitedBitmapPtr.bitmap_isSet(childId)) {
                    status = workQueuePtr.queue_push(childId);
                }
            }
        }
        return false;
    }

    public boolean isCycle(Vector_t nodeVectorPtr, NetNode nodePtr) {
        if (nodePtr.mark == NET_NODE_MARK_INIT) {
            nodePtr.mark = NET_NODE_MARK_TEST;
            IntList childIdListPtr = nodePtr.childIdListPtr;
            IntListNode it = childIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int childId = it.dataPtr;
                NetNode childNodePtr = (NetNode) (nodeVectorPtr.vector_at(childId));
                if (isCycle(nodeVectorPtr, childNodePtr)) {
                    return true;
                }
            }
        } else if (nodePtr.mark == NET_NODE_MARK_TEST) {
            return true;
        } else if (nodePtr.mark == NET_NODE_MARK_DONE) {
            return false;
        }
        nodePtr.mark = NET_NODE_MARK_DONE;
        return false;
    }

    public boolean net_isCycle() {
        int numNode = nodeVectorPtr.vector_getSize();
        for (int n = 0; n < numNode; n++) {
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(n));
            nodePtr.mark = NET_NODE_MARK_INIT;
        }
        for (int n = 0; n < numNode; n++) {
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(n));
            if (nodePtr.mark == NET_NODE_MARK_INIT) {
                if (isCycle(nodeVectorPtr, nodePtr)) return true;
            } else if (nodePtr.mark == NET_NODE_MARK_DONE) {
                ;
            }
        }
        return false;
    }

    public IntList net_getParentIdListPtr(int id) {
        NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(id));
        return nodePtr.parentIdListPtr;
    }

    public IntList net_getChildIdListPtr(int id) {
        NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(id));
        return nodePtr.childIdListPtr;
    }

    public boolean net_findAncestors(int id, BitMap ancestorBitmapPtr, Queue workQueuePtr) {
        boolean status;
        ancestorBitmapPtr.bitmap_clearAll();
        workQueuePtr.queue_clear();
        {
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(id));
            IntList parentIdListPtr = nodePtr.parentIdListPtr;
            IntListNode it = parentIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int parentId = it.dataPtr;
                status = ancestorBitmapPtr.bitmap_set(parentId);
                status = workQueuePtr.queue_push(parentId);
            }
        }
        while (!workQueuePtr.queue_isEmpty()) {
            int parentId = workQueuePtr.queue_pop();
            if (parentId == id) {
                workQueuePtr.queue_clear();
                return false;
            }
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(parentId));
            IntList grandParentIdListPtr = nodePtr.parentIdListPtr;
            IntListNode it = grandParentIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int grandParentId = it.dataPtr;
                if (!ancestorBitmapPtr.bitmap_isSet(grandParentId)) {
                    status = ancestorBitmapPtr.bitmap_set(grandParentId);
                    status = workQueuePtr.queue_push(grandParentId);
                }
            }
        }
        return true;
    }

    public boolean net_findDescendants(int id, BitMap descendantBitmapPtr, Queue workQueuePtr) {
        boolean status;
        descendantBitmapPtr.bitmap_clearAll();
        workQueuePtr.queue_clear();
        {
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(id));
            IntList childIdListPtr = nodePtr.childIdListPtr;
            IntListNode it = childIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int childId = it.dataPtr;
                status = descendantBitmapPtr.bitmap_set(childId);
                status = workQueuePtr.queue_push(childId);
            }
        }
        while (!workQueuePtr.queue_isEmpty()) {
            int childId = workQueuePtr.queue_pop();
            if (childId == id) {
                workQueuePtr.queue_clear();
                return false;
            }
            NetNode nodePtr = (NetNode) (nodeVectorPtr.vector_at(childId));
            IntList grandChildIdListPtr = nodePtr.childIdListPtr;
            IntListNode it = grandChildIdListPtr.head;
            while (it.nextPtr != null) {
                it = it.nextPtr;
                int grandChildId = it.dataPtr;
                if (!descendantBitmapPtr.bitmap_isSet(grandChildId)) {
                    status = descendantBitmapPtr.bitmap_set(grandChildId);
                    status = workQueuePtr.queue_push(grandChildId);
                }
            }
        }
        return true;
    }

    public void net_generateRandomEdges(int maxNumParent, int percentParent, Random randomPtr) {
        int numNode = nodeVectorPtr.vector_getSize();
        BitMap visitedBitmapPtr = BitMap.bitmap_alloc(numNode);
        Queue workQueuePtr = Queue.queue_alloc(-1);
        for (int n = 0; n < numNode; n++) {
            for (int p = 0; p < maxNumParent; p++) {
                int value = (int) (randomPtr.random_generate() % 100);
                if (value < percentParent) {
                    int parent = (int) (randomPtr.random_generate() % numNode);
                    if ((parent != n) && !net_hasEdge(parent, n) && !net_isPath(n, parent, visitedBitmapPtr, workQueuePtr)) {
                        insertEdge(parent, n);
                    }
                }
            }
        }
        visitedBitmapPtr.bitmap_free();
        workQueuePtr.queue_free();
    }
}
