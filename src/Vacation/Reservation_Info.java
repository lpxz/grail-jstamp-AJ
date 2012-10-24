package Vacation;

public class Reservation_Info {

    int id;

    int type;

    int price;

    public Reservation_Info(int type, int id, int price) {
        this.type = type;
        this.id = id;
        this.price = price;
    }

    public static int reservation_info_compare(Reservation_Info aPtr, Reservation_Info bPtr) {
        int typeDiff;
        typeDiff = aPtr.type - bPtr.type;
        return ((typeDiff != 0) ? (typeDiff) : (aPtr.id - bPtr.id));
    }
}
