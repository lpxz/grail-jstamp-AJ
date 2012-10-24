package Vacation;

public class Customer {

    int id;

    List_t reservationInfoListPtr;

    public Customer(int id) {
        this.id = id;
        reservationInfoListPtr = new List_t();
    }

    int customer_compare(Customer aPtr, Customer bPtr) {
        return (aPtr.id - bPtr.id);
    }

    boolean customer_addReservationInfo(int type, int id, int price) {
        Reservation_Info reservationInfoPtr = new Reservation_Info(type, id, price);
        return reservationInfoListPtr.insert(reservationInfoPtr);
    }

    boolean customer_removeReservationInfo(int type, int id) {
        Reservation_Info findReservationInfo = new Reservation_Info(type, id, 0);
        Reservation_Info reservationInfoPtr = (Reservation_Info) reservationInfoListPtr.find(findReservationInfo);
        if (reservationInfoPtr == null) {
            return false;
        }
        boolean status = reservationInfoListPtr.remove(findReservationInfo);
        return true;
    }

    int customer_getBill() {
        int bill = 0;
        List_Node it;
        it = reservationInfoListPtr.head;
        while (it.nextPtr != null) {
            it = it.nextPtr;
            Reservation_Info reservationInfoPtr = (Reservation_Info) it.dataPtr;
            bill += reservationInfoPtr.price;
        }
        return bill;
    }
}
