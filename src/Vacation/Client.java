package Vacation;

import Yada.java.Barrier;
import common.Random;

public class Client extends Thread {

    public static final int RESERVATION_CAR = 994;

    public static final int RESERVATION_FLIGHT = 995;

    public static final int RESERVATION_ROOM = 996;

    public static final int NUM_RESERVATION_TYPE = 997;

    public static int ACTION_MAKE_RESERVATION = 0;

    public static int ACTION_DELETE_CUSTOMER = 1;

    public static int ACTION_UPDATE_TABLES = 2;

    int id;

    Manager managerPtr;

    Random randomPtr;

    int numOperation;

    int numQueryPerTransaction;

    int queryRange;

    int percentUser;

    public Client() {
    }

    public Client(int id, Manager managerPtr, int numOperation, int numQueryPerTransaction, int queryRange, int percentUser) {
        this.randomPtr = new Random();
        this.randomPtr.random_alloc();
        this.id = id;
        this.managerPtr = managerPtr;
        randomPtr.random_seed(id);
        this.numOperation = numOperation;
        this.numQueryPerTransaction = numQueryPerTransaction;
        this.queryRange = queryRange;
        this.percentUser = percentUser;
    }

    public int selectAction(int r, int percentUser) {
        if (r < percentUser) {
            return ACTION_MAKE_RESERVATION;
        } else if ((r & 1) == 1) {
            return ACTION_DELETE_CUSTOMER;
        } else {
            return ACTION_UPDATE_TABLES;
        }
    }

    public void run() {
        int myId = id;
        Manager managerPtr = this.managerPtr;
        Random randomPtr = this.randomPtr;
        int numOperation = this.numOperation;
        int numQueryPerTransaction = this.numQueryPerTransaction;
        int queryRange = this.queryRange;
        int percentUser = this.percentUser;
        int types[] = new int[numQueryPerTransaction];
        int ids[] = new int[numQueryPerTransaction];
        int ops[] = new int[numQueryPerTransaction];
        int prices[] = new int[numQueryPerTransaction];
        for (int i = 0; i < numOperation; i++) {
            int r = randomPtr.posrandom_generate() % 100;
            int action = selectAction(r, percentUser);
            if (action == ACTION_MAKE_RESERVATION) {
                int maxPrices[] = new int[NUM_RESERVATION_TYPE];
                int maxIds[] = new int[NUM_RESERVATION_TYPE];
                maxPrices[0] = -1;
                maxPrices[1] = -1;
                maxPrices[2] = -1;
                maxIds[0] = -1;
                maxIds[1] = -1;
                maxIds[2] = -1;
                int n;
                int numQuery = randomPtr.posrandom_generate() % numQueryPerTransaction + 1;
                int customerId = randomPtr.posrandom_generate() % queryRange + 1;
                for (n = 0; n < numQuery; n++) {
                    types[n] = randomPtr.random_generate() % NUM_RESERVATION_TYPE;
                    ids[n] = (randomPtr.random_generate() % queryRange) + 1;
                }
                boolean isFound = false;
                synchronized (common.G.lock) {
                    for (n = 0; n < numQuery; n++) {
                        int t = types[n];
                        int id = ids[n];
                        int price = -1;
                        if (t == RESERVATION_CAR) {
                            if (managerPtr.manager_queryCar(id) >= 0) {
                                price = managerPtr.manager_queryCarPrice(id);
                            }
                        } else if (t == RESERVATION_FLIGHT) {
                            if (managerPtr.manager_queryFlight(id) >= 0) {
                                price = managerPtr.manager_queryFlightPrice(id);
                            }
                        } else if (t == RESERVATION_ROOM) {
                            if (managerPtr.manager_queryRoom(id) >= 0) {
                                price = managerPtr.manager_queryRoomPrice(id);
                            }
                        }
                        if (price > maxPrices[t]) {
                            maxPrices[t] = price;
                            maxIds[t] = id;
                            isFound = true;
                        }
                    }
                    if (isFound) {
                        managerPtr.manager_addCustomer(customerId);
                    }
                    if (maxIds[RESERVATION_CAR] > 0) {
                        managerPtr.manager_reserveCar(customerId, maxIds[RESERVATION_CAR]);
                    }
                    if (maxIds[RESERVATION_FLIGHT] > 0) {
                        managerPtr.manager_reserveFlight(customerId, maxIds[RESERVATION_FLIGHT]);
                    }
                    if (maxIds[RESERVATION_ROOM] > 0) {
                        managerPtr.manager_reserveRoom(customerId, maxIds[RESERVATION_ROOM]);
                    }
                }
            } else if (action == ACTION_DELETE_CUSTOMER) {
                int customerId = randomPtr.posrandom_generate() % queryRange + 1;
                synchronized (common.G.lock) {
                    int bill = managerPtr.manager_queryCustomerBill(customerId);
                    if (bill >= 0) {
                        managerPtr.manager_deleteCustomer(customerId);
                    }
                }
            } else if (action == ACTION_UPDATE_TABLES) {
                int numUpdate = randomPtr.posrandom_generate() % numQueryPerTransaction + 1;
                int n;
                for (n = 0; n < numUpdate; n++) {
                    types[n] = randomPtr.posrandom_generate() % NUM_RESERVATION_TYPE;
                    ids[n] = (randomPtr.posrandom_generate() % queryRange) + 1;
                    ops[n] = randomPtr.posrandom_generate() % 2;
                    if (ops[n] == 1) {
                        prices[n] = ((randomPtr.posrandom_generate() % 5) * 10) + 50;
                    }
                }
                synchronized (common.G.lock) {
                    for (n = 0; n < numUpdate; n++) {
                        int t = types[n];
                        int id = ids[n];
                        int doAdd = ops[n];
                        if (doAdd == 1) {
                            int newPrice = prices[n];
                            if (t == RESERVATION_CAR) {
                                managerPtr.manager_addCar(id, 100, newPrice);
                            } else if (t == RESERVATION_FLIGHT) {
                                managerPtr.manager_addFlight(id, 100, newPrice);
                            } else if (t == RESERVATION_ROOM) {
                                managerPtr.manager_addRoom(id, 100, newPrice);
                            }
                        } else {
                            if (t == RESERVATION_CAR) {
                                managerPtr.manager_deleteCar(id, 100);
                            } else if (t == RESERVATION_FLIGHT) {
                                managerPtr.manager_deleteFlight(id);
                            } else if (t == RESERVATION_ROOM) {
                                managerPtr.manager_deleteRoom(id, 100);
                            }
                        }
                    }
                }
            }
        }
        Barrier.enterBarrier();
    }
}
