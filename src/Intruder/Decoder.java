package Intruder;

public class Decoder {

    RBTree fragmentedMapPtr;

    Queue_t decodedQueuePtr;

    int cnt;

    public Decoder() {
        fragmentedMapPtr = new RBTree(0);
        decodedQueuePtr = new Queue_t(1024);
    }

    public int process(Packet packetPtr, int numByte) {
        boolean status;
        ERROR er = new ERROR();
        if (numByte < 0) {
            return er.SHORT;
        }
        int flowId = packetPtr.flowId;
        int fragmentId = packetPtr.fragmentId;
        int numFragment = packetPtr.numFragment;
        int length = packetPtr.length;
        if (flowId < 0) {
            return er.FLOWID;
        }
        if ((fragmentId < 0) || (fragmentId >= numFragment)) {
            return er.FRAGMENTID;
        }
        if (length < 0) {
            return er.LENGTH;
        }
        if (numFragment > 1) {
            List_t fragmentListPtr = (List_t) fragmentedMapPtr.get(flowId);
            if (fragmentListPtr == null) {
                fragmentListPtr = new List_t(1);
                status = fragmentListPtr.insert(packetPtr);
                status = fragmentedMapPtr.insert(flowId, fragmentListPtr);
            } else {
                List_Iter it = new List_Iter();
                it.reset(fragmentListPtr);
                System.out.print("");
                Packet firstFragmentPtr = (Packet) it.next(fragmentListPtr);
                int expectedNumFragment = firstFragmentPtr.numFragment;
                if (numFragment != expectedNumFragment) {
                    status = fragmentedMapPtr.deleteNode(flowId);
                    return er.NUMFRAGMENT;
                }
                status = fragmentListPtr.insert(packetPtr);
                if (fragmentListPtr.getSize() == numFragment) {
                    int numBytes = 0;
                    int i = 0;
                    it.reset(fragmentListPtr);
                    while (it.hasNext(fragmentListPtr)) {
                        Packet fragmentPtr = (Packet) it.next(fragmentListPtr);
                        if (fragmentPtr.fragmentId != i) {
                            status = fragmentedMapPtr.deleteNode(flowId);
                            return er.INCOMPLETE;
                        }
                        numBytes = numBytes + fragmentPtr.length;
                        i++;
                    }
                    byte[] data = new byte[numBytes];
                    it.reset(fragmentListPtr);
                    int index = 0;
                    while (it.hasNext(fragmentListPtr)) {
                        Packet fragmentPtr = (Packet) it.next(fragmentListPtr);
                        for (i = 0; i < fragmentPtr.length; i++) {
                            data[index++] = fragmentPtr.data[i];
                        }
                    }
                    Decoded decodedPtr = new Decoded();
                    decodedPtr.flowId = flowId;
                    decodedPtr.data = data;
                    status = decodedQueuePtr.queue_push(decodedPtr);
                    status = fragmentedMapPtr.deleteNode(flowId);
                }
            }
        } else {
            if (fragmentId != 0) {
                return er.FRAGMENTID;
            }
            byte[] data = packetPtr.data;
            Decoded decodedPtr = new Decoded();
            decodedPtr.flowId = flowId;
            decodedPtr.data = data;
            status = decodedQueuePtr.queue_push(decodedPtr);
        }
        return er.NONE;
    }

    public byte[] getComplete(int[] decodedFlowId) {
        byte[] data;
        Decoded decodedPtr = (Decoded) decodedQueuePtr.queue_pop();
        if (decodedPtr != null) {
            decodedFlowId[0] = decodedPtr.flowId;
            data = decodedPtr.data;
        } else {
            decodedFlowId[0] = -1;
            data = null;
        }
        return data;
    }
}
