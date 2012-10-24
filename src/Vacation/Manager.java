package Vacation;

public class Manager {

    public static final int RESERVATION_CAR = 994;

    public static final int RESERVATION_FLIGHT = 995;

    public static final int RESERVATION_ROOM = 996;

    public static final int NUM_RESERVATION_TYPE = 997;

    RBTree carTablePtr;

    RBTree roomTablePtr;

    RBTree flightTablePtr;

    RBTree customerTablePtr;

    public Manager() {
        carTablePtr = new RBTree();
        roomTablePtr = new RBTree();
        flightTablePtr = new RBTree();
        customerTablePtr = new RBTree();
    }

    boolean addReservation(RBTree tablePtr, int id, int num, int price) {
        Reservation reservationPtr;
        reservationPtr = (Reservation) tablePtr.find(id);
        if (reservationPtr == null) {
            if (num < 1 || price < 0) {
                return false;
            }
            reservationPtr = new Reservation(id, num, price);
            tablePtr.insert(id, reservationPtr);
        } else {
            if (!reservationPtr.reservation_addToTotal(num)) {
                return false;
            }
            if (reservationPtr.numTotal == 0) {
                boolean status = tablePtr.remove(id);
            } else {
                reservationPtr.reservation_updatePrice(price);
            }
        }
        return true;
    }

    boolean manager_addCar(int carId, int numCars, int price) {
        return addReservation(carTablePtr, carId, numCars, price);
    }

    boolean manager_deleteCar(int carId, int numCar) {
        return addReservation(carTablePtr, carId, -numCar, -1);
    }

    boolean manager_addRoom(int roomId, int numRoom, int price) {
        return addReservation(roomTablePtr, roomId, numRoom, price);
    }

    boolean manager_deleteRoom(int roomId, int numRoom) {
        return addReservation(roomTablePtr, roomId, -numRoom, -1);
    }

    boolean manager_addFlight(int flightId, int numSeat, int price) {
        return addReservation(flightTablePtr, flightId, numSeat, price);
    }

    boolean manager_deleteFlight(int flightId) {
        Reservation reservationPtr = (Reservation) flightTablePtr.find(flightId);
        if (reservationPtr == null) {
            return false;
        }
        if (reservationPtr.numUsed > 0) {
            return false;
        }
        return addReservation(flightTablePtr, flightId, -reservationPtr.numTotal, -1);
    }

    boolean manager_addCustomer(int customerId) {
        Customer customerPtr;
        boolean status;
        if (customerTablePtr.contains(customerId)) {
            return false;
        }
        customerPtr = new Customer(customerId);
        status = customerTablePtr.insert(customerId, customerPtr);
        return true;
    }

    boolean manager_deleteCustomer(int customerId) {
        Customer customerPtr;
        RBTree reservationTables[] = new RBTree[NUM_RESERVATION_TYPE];
        List_t reservationInfoListPtr;
        List_Node it;
        boolean status;
        customerPtr = (Customer) customerTablePtr.find(customerId);
        if (customerPtr == null) {
            return false;
        }
        reservationTables[RESERVATION_CAR] = carTablePtr;
        reservationTables[RESERVATION_ROOM] = roomTablePtr;
        reservationTables[RESERVATION_FLIGHT] = flightTablePtr;
        reservationInfoListPtr = customerPtr.reservationInfoListPtr;
        it = reservationInfoListPtr.head;
        while (it.nextPtr != null) {
            Reservation_Info reservationInfoPtr;
            Reservation reservationPtr;
            it = it.nextPtr;
            reservationInfoPtr = (Reservation_Info) it.dataPtr;
            reservationPtr = (Reservation) reservationTables[reservationInfoPtr.type].find(reservationInfoPtr.id);
            status = reservationPtr.reservation_cancel();
        }
        status = customerTablePtr.remove(customerId);
        return true;
    }

    int queryNumFree(RBTree tablePtr, int id) {
        int numFree = -1;
        Reservation reservationPtr = (Reservation) tablePtr.find(id);
        if (reservationPtr != null) {
            numFree = reservationPtr.numFree;
        }
        return numFree;
    }

    int queryPrice(RBTree tablePtr, int id) {
        int price = -1;
        Reservation reservationPtr = (Reservation) tablePtr.find(id);
        if (reservationPtr != null) {
            price = reservationPtr.price;
        }
        return price;
    }

    int manager_queryCar(int carId) {
        return queryNumFree(carTablePtr, carId);
    }

    int manager_queryCarPrice(int carId) {
        return queryPrice(carTablePtr, carId);
    }

    int manager_queryRoom(int roomId) {
        return queryNumFree(roomTablePtr, roomId);
    }

    int manager_queryRoomPrice(int roomId) {
        return queryPrice(roomTablePtr, roomId);
    }

    int manager_queryFlight(int flightId) {
        return queryNumFree(flightTablePtr, flightId);
    }

    int manager_queryFlightPrice(int flightId) {
        return queryPrice(flightTablePtr, flightId);
    }

    int manager_queryCustomerBill(int customerId) {
        int bill = -1;
        Customer customerPtr;
        customerPtr = (Customer) customerTablePtr.find(customerId);
        if (customerPtr != null) {
            bill = customerPtr.customer_getBill();
        }
        return bill;
    }

    static boolean reserve(RBTree tablePtr, RBTree customerTablePtr, int customerId, int id, int type) {
        Customer customerPtr;
        Reservation reservationPtr;
        customerPtr = (Customer) customerTablePtr.find(customerId);
        if (customerPtr == null) {
            return false;
        }
        reservationPtr = (Reservation) tablePtr.find(id);
        if (reservationPtr == null) {
            return false;
        }
        if (!reservationPtr.reservation_make()) {
            return false;
        }
        if (!customerPtr.customer_addReservationInfo(type, id, reservationPtr.price)) {
            boolean status = reservationPtr.reservation_cancel();
            return false;
        }
        return true;
    }

    boolean manager_reserveCar(int customerId, int carId) {
        return reserve(carTablePtr, customerTablePtr, customerId, carId, RESERVATION_CAR);
    }

    boolean manager_reserveRoom(int customerId, int roomId) {
        return reserve(roomTablePtr, customerTablePtr, customerId, roomId, RESERVATION_ROOM);
    }

    boolean manager_reserveFlight(int customerId, int flightId) {
        return reserve(flightTablePtr, customerTablePtr, customerId, flightId, RESERVATION_FLIGHT);
    }

    static boolean cancel(RBTree tablePtr, RBTree customerTablePtr, int customerId, int id, int type) {
        Customer customerPtr;
        Reservation reservationPtr;
        customerPtr = (Customer) customerTablePtr.find(customerId);
        if (customerPtr == null) {
            return false;
        }
        reservationPtr = (Reservation) tablePtr.find(id);
        if (reservationPtr == null) {
            return false;
        }
        if (!reservationPtr.reservation_cancel()) {
            return false;
        }
        if (!customerPtr.customer_removeReservationInfo(type, id)) {
            boolean status = reservationPtr.reservation_make();
            return false;
        }
        return true;
    }

    boolean manager_cancelCar(int customerId, int carId) {
        return cancel(carTablePtr, customerTablePtr, customerId, carId, RESERVATION_CAR);
    }

    boolean manager_cancelRoom(int customerId, int roomId) {
        return cancel(roomTablePtr, customerTablePtr, customerId, roomId, RESERVATION_ROOM);
    }

    boolean manager_cancelFlight(int customerId, int flightId) {
        return cancel(flightTablePtr, customerTablePtr, customerId, flightId, RESERVATION_FLIGHT);
    }
}
